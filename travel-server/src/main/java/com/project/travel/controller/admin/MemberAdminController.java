package com.project.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.travel.domain.Result;
import com.project.travel.domain.SysMember;
import com.project.travel.domain.SysPointsLog;
import com.project.travel.domain.User;
import com.project.travel.service.SysMemberService;
import com.project.travel.service.SysPointsLogService;
import com.project.travel.service.UserService;
import com.project.travel.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("admin/member")
public class MemberAdminController {

    @Autowired
    private SysMemberService memberService;

    @Autowired
    private SysPointsLogService pointsLogService;

    @Autowired
    private UserService userService;

    @PostMapping("getSysMemberPage")
    public Result getSysMemberPage(@RequestBody SysMember query) {
        Page<SysMember> page = new Page<>(query.getPageNumber(), query.getPageSize());
        QueryWrapper<SysMember> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(query.getUserName())) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.lambda().like(User::getUserName, query.getUserName());
            List<User> users = userService.list(userWrapper);
            if (users.isEmpty()) {
                return Result.success(new Page<SysMember>(query.getPageNumber(), query.getPageSize()));
            }
            wrapper.lambda().in(SysMember::getUserId, users.stream().map(User::getId).collect(Collectors.toList()));
        }
        wrapper.lambda()
                .eq(query.getLevel() != null, SysMember::getLevel, query.getLevel())
                .orderByDesc(SysMember::getCreateTime);
        Page<SysMember> result = memberService.page(page, wrapper);
        for (SysMember member : result.getRecords()) {
            if (StringUtils.isNotBlank(member.getUserId())) {
                User user = userService.getById(member.getUserId());
                if (user != null) {
                    member.setUserName(user.getUserName());
                }
            }
        }
        return Result.success(result);
    }

    @GetMapping("getPointsLog")
    public Result getPointsLog(@RequestParam String userId,
                               @RequestParam(defaultValue = "1") Integer pageNumber,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysPointsLog> page = new Page<>(pageNumber, pageSize);
        QueryWrapper<SysPointsLog> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SysPointsLog::getUserId, userId).orderByDesc(SysPointsLog::getCreateTime);
        return Result.success(pointsLogService.page(page, wrapper));
    }

    @PostMapping("adjustPoints")
    public Result adjustPoints(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        Integer changePoints = params.get("changePoints") instanceof Number
                ? ((Number) params.get("changePoints")).intValue() : null;
        String reason = (String) params.get("reason");
        if (StringUtils.isBlank(userId) || changePoints == null || changePoints == 0) {
            return Result.fail("参数不完整");
        }
        String operatorId = TokenUtils.getUserIdByToken();
        boolean ok = memberService.adjustPoints(userId, changePoints, reason, operatorId);
        return ok ? Result.success() : Result.fail("调整失败，积分余额不足");
    }

    @GetMapping("stats")
    public Result stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("total", memberService.count());
        data.put("vip", memberService.count(new QueryWrapper<SysMember>().lambda().eq(SysMember::getLevel, 1)));
        data.put("svip", memberService.count(new QueryWrapper<SysMember>().lambda().eq(SysMember::getLevel, 2)));
        return Result.success(data);
    }
}
