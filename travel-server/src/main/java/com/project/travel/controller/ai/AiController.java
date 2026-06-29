package com.project.travel.controller.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.*;
import com.project.travel.enums.ResultCode;
import com.project.travel.constant.PointsConstants;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.AiUsageService;
import com.project.travel.service.SparkAiService;
import com.project.travel.service.SysAttractionsService;
import com.project.travel.service.SysHotelService;
import com.project.travel.service.SysTravelPlanService;
import com.project.travel.service.SysTravelShareService;
import com.project.travel.service.UserService;
import com.project.travel.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("ai")
public class AiController {

    @Autowired
    private SparkAiService sparkAiService;

    @Autowired
    private SysTravelPlanService sysTravelPlanService;

    @Autowired
    private SysAttractionsService sysAttractionsService;

    @Autowired
    private SysHotelService sysHotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private AiUsageService aiUsageService;

    @Autowired
    private SysMemberService sysMemberService;

    @Autowired
    private SysTravelShareService sysTravelShareService;

    @PostMapping(value = "chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AiChatRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("X-Accel-Buffering", "no");

        if (request == null || StringUtils.isBlank(request.getQuestion())) {
            SseEmitter emitter = new SseEmitter(0L);
            try {
                sendStreamEvent(emitter, "error", "问题不能为空");
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        // 异步线程中无法读取 RequestContextHolder，需在主线程先解析用户
        String userId = TokenUtils.getUserIdByToken();
        User currentUser = StringUtils.isNotBlank(userId) ? userService.getById(userId) : null;
        String userContext = buildUserContext(currentUser);
        if (currentUser != null) {
            aiUsageService.logChat(currentUser.getId(), request.getQuestion());
        }

        SseEmitter emitter = new SseEmitter(120000L);
        CompletableFuture.runAsync(() -> {
            try {
                sparkAiService.chatStream(
                        request.getQuestion(),
                        request.getHistory(),
                        userContext,
                        (content, status) -> {
                            try {
                                sendStreamEvent(emitter, "message", buildStreamPayload(content, status));
                            } catch (IOException ioException) {
                                throw new RuntimeException(ioException);
                            }
                        }
                );
                if (currentUser != null) {
                    int aiPoints = sysMemberService.awardPointsDaily(
                            currentUser.getId(),
                            PointsConstants.AI_CHAT,
                            PointsConstants.TYPE_INTERACTION,
                            PointsConstants.DESC_AI_CHAT,
                            PointsConstants.AI_CHAT_DAILY_LIMIT);
                    if (aiPoints > 0) {
                        Map<String, Object> pointsData = new HashMap<>();
                        pointsData.put("pointsEarned", aiPoints);
                        sendStreamEvent(emitter, "points", pointsData);
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendStreamEvent(emitter, "error", e.getMessage());
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        });
        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    @PostMapping("chat")
    public Result chat(@RequestBody AiChatRequest request) {
        try {
            if (request == null || StringUtils.isBlank(request.getQuestion())) {
                return Result.fail("问题不能为空");
            }
            User user = getCurrentUser();
            String userContext = buildUserContext(user);
            if (user != null) {
                aiUsageService.logChat(user.getId(), request.getQuestion());
            }
            String answer = sparkAiService.chat(request.getQuestion(), request.getHistory(), userContext);

            Map<String, Object> data = new HashMap<>();
            data.put("answer", answer);
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("AI 对话失败：" + e.getMessage());
        }
    }

    @PostMapping("plan/generate")
    public Result generatePlan(@RequestBody SysTravelPlan plan) {
        try {
            User user = requireLoginUser();
            if (StringUtils.isBlank(plan.getDestination())) {
                return Result.fail("请输入目的地");
            }
            if (plan.getDays() == null || plan.getDays() <= 0) {
                return Result.fail("游玩天数不正确");
            }

            List<SysAttractions> attractions = queryRecommendAttractions(plan);
            List<SysHotel> hotels = queryRecommendHotels(plan, attractions);
            String prompt = sparkAiService.generatePlanPrompt(plan, attractions);
            String answer = sparkAiService.chat(prompt, null, buildUserContext(user));

            plan.setUserId(user.getId());
            plan.setTitle(plan.getDestination() + plan.getDays() + "日游");
            if (StringUtils.isBlank(plan.getPreferences())) {
                plan.setPreferences(user.getScenicType());
            }
            plan.setPlanContent(answer);
            plan.setRecommendAttractions(JSON.toJSONString(buildRecommendSummary(attractions)));
            plan.setRecommendHotels(JSON.toJSONString(buildHotelSummary(hotels)));

            boolean saved = sysTravelPlanService.save(plan);
            if (!saved) {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
            aiUsageService.logPlan(user.getId(), plan.getDestination());
            return Result.success(plan);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成行程失败：" + e.getMessage());
        }
    }

    @PostMapping("plan/share")
    public Result sharePlan(@RequestParam("id") String id) {
        try {
            User user = requireLoginUser();
            SysTravelPlan plan = sysTravelPlanService.getById(id);
            if (plan == null) {
                return Result.fail("行程不存在");
            }
            if (!user.getId().equals(plan.getUserId())) {
                return Result.fail("无权分享该行程");
            }

            SysTravelShare share = new SysTravelShare();
            share.setTitle(StringUtils.defaultIfBlank(plan.getTitle(), plan.getDestination() + "行程分享"));
            share.setContent(buildPlanShareContent(plan));
            share.setLocation(plan.getDestination());
            share.setType(3);
            share.setUserId(user.getId());
            share.setAvatar(user.getAvatar());
            share.setLikes(0);
            share.setCreateTime(new Date());
            share.setUpdateTime(new Date());

            if (!sysTravelShareService.save(share)) {
                return Result.fail("分享失败");
            }
            return Result.success(share);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("分享失败：" + e.getMessage());
        }
    }

    @PostMapping("plan/getMyPlanPage")
    public Result getMyPlanPage(@RequestBody SysTravelPlan plan) {
        try {
            User user = requireLoginUser();
            Page<SysTravelPlan> page = new Page<>(plan.getPageNumber() == null ? 1 : plan.getPageNumber(),
                    plan.getPageSize() == null ? 10 : plan.getPageSize());
            QueryWrapper<SysTravelPlan> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(SysTravelPlan::getUserId, user.getId())
                    .orderByDesc(SysTravelPlan::getCreateTime);
            return Result.success(sysTravelPlanService.page(page, queryWrapper));
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("plan/getById")
    public Result getPlanById(@RequestParam("id") String id) {
        try {
            User user = requireLoginUser();
            SysTravelPlan plan = sysTravelPlanService.getById(id);
            if (plan == null) {
                return Result.fail("行程不存在");
            }
            if (!user.getId().equals(plan.getUserId())) {
                return Result.fail("无权查看该行程");
            }
            return Result.success(plan);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("plan/remove")
    public Result removePlan(@RequestParam("id") String id) {
        try {
            User user = requireLoginUser();
            SysTravelPlan plan = sysTravelPlanService.getById(id);
            if (plan == null) {
                return Result.fail("行程不存在");
            }
            if (!user.getId().equals(plan.getUserId())) {
                return Result.fail("无权删除该行程");
            }
            return sysTravelPlanService.removeById(id) ? Result.success() : Result.fail("删除失败");
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        }
    }

    private User requireLoginUser() {
        String userId = TokenUtils.getUserIdByToken();
        if (StringUtils.isBlank(userId)) {
            throw new IllegalStateException("请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new IllegalStateException("用户不存在，请重新登录");
        }
        return user;
    }

    private Map<String, Object> buildStreamPayload(String content, Integer status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        payload.put("status", status);
        return payload;
    }

    private void sendStreamEvent(SseEmitter emitter, String eventName, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(eventName).data(data));
    }

    private User getCurrentUser() {
        String userId = TokenUtils.getUserIdByToken();
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return userService.getById(userId);
    }

    private String buildUserContext(User user) {
        if (user == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(user.getUserName())) {
            parts.add("昵称=" + user.getUserName());
        }
        if (StringUtils.isNotBlank(user.getScenicType())) {
            parts.add("喜好类型=" + user.getScenicType());
        }
        if (StringUtils.isNotBlank(user.getLocation())) {
            parts.add("所在地区=" + user.getLocation());
        }
        return String.join("，", parts);
    }

    private String buildPlanShareContent(SysTravelPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AI 智能行程】").append('\n');
        if (StringUtils.isNotBlank(plan.getDepartureDate())) {
            sb.append("出发时间：").append(plan.getDepartureDate()).append('\n');
        }
        sb.append("目的地：").append(plan.getDestination()).append('\n');
        sb.append("游玩天数：").append(plan.getDays()).append("天\n");
        if (StringUtils.isNotBlank(plan.getBudget())) {
            sb.append("预算：").append(plan.getBudget()).append('\n');
        }
        if (StringUtils.isNotBlank(plan.getPreferences())) {
            sb.append("偏好：").append(plan.getPreferences()).append('\n');
        }
        sb.append('\n').append(StringUtils.defaultString(plan.getPlanContent()));
        String content = sb.toString();
        if (content.length() > 5000) {
            return content.substring(0, 5000) + "...";
        }
        return content;
    }

    private List<SysAttractions> queryRecommendAttractions(SysTravelPlan plan) {
        QueryWrapper<SysAttractions> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysAttractions::getState, 1);

        if (StringUtils.isNotBlank(plan.getPreferences())) {
            String[] types = plan.getPreferences().split(",");
            queryWrapper.lambda().and(wrapper -> {
                for (int i = 0; i < types.length; i++) {
                    String type = types[i];
                    if (StringUtils.isNotBlank(type)) {
                        if (i == 0) {
                            wrapper.eq(SysAttractions::getScenicType, type.trim());
                        } else {
                            wrapper.or().eq(SysAttractions::getScenicType, type.trim());
                        }
                    }
                }
            });
        }

        if (StringUtils.isNotBlank(plan.getDestination())) {
            queryWrapper.lambda().like(SysAttractions::getLocation, plan.getDestination());
        }

        queryWrapper.lambda()
                .orderByDesc(SysAttractions::getPv)
                .last("limit 5");
        return sysAttractionsService.list(queryWrapper);
    }

    private List<JSONObject> buildRecommendSummary(List<SysAttractions> attractions) {
        if (attractions == null) {
            return new ArrayList<>();
        }
        return attractions.stream().map(item -> {
            JSONObject json = new JSONObject();
            json.put("id", item.getId());
            json.put("name", item.getName());
            json.put("location", item.getLocation());
            json.put("introduce", item.getIntroduce());
            json.put("images", item.getImages());
            json.put("price", item.getPrice());
            return json;
        }).collect(Collectors.toList());
    }

    private List<SysHotel> queryRecommendHotels(SysTravelPlan plan, List<SysAttractions> attractions) {
        QueryWrapper<SysHotel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysHotel::getState, 1);

        if (attractions != null && !attractions.isEmpty()) {
            queryWrapper.lambda().and(wrapper -> {
                for (int i = 0; i < attractions.size(); i++) {
                    SysAttractions item = attractions.get(i);
                    if (StringUtils.isNotBlank(item.getName())) {
                        if (i == 0) {
                            wrapper.like(SysHotel::getAttractions, item.getName());
                        } else {
                            wrapper.or().like(SysHotel::getAttractions, item.getName());
                        }
                    }
                }
            });
        } else if (StringUtils.isNotBlank(plan.getDestination())) {
            queryWrapper.lambda().and(wrapper -> wrapper
                    .like(SysHotel::getAttractions, plan.getDestination())
                    .or().like(SysHotel::getAddress, plan.getDestination())
                    .or().like(SysHotel::getName, plan.getDestination()));
        }

        queryWrapper.lambda()
                .orderByDesc(SysHotel::getCreateTime)
                .last("limit 5");
        return sysHotelService.list(queryWrapper);
    }

    private List<JSONObject> buildHotelSummary(List<SysHotel> hotels) {
        if (hotels == null) {
            return new ArrayList<>();
        }
        return hotels.stream().map(item -> {
            JSONObject json = new JSONObject();
            json.put("id", item.getId());
            json.put("name", item.getName());
            json.put("attractions", item.getAttractions());
            json.put("address", item.getAddress());
            json.put("introduce", item.getIntroduce());
            json.put("images", item.getImages());
            return json;
        }).collect(Collectors.toList());
    }
}
