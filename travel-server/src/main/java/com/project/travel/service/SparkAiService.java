package com.project.travel.service;

import com.project.travel.config.SparkConfig;
import com.project.travel.domain.AiChatMessage;
import com.project.travel.domain.SysAttractions;
import com.project.travel.domain.SysTravelPlan;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.listener.SparkBaseListener;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkRequestBuilder;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkResponse;
import io.github.briqt.spark4j.model.response.SparkResponseUsage;
import okhttp3.Response;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

@Service
public class SparkAiService {

    private static final String SYSTEM_PROMPT = "你是「个性化旅游规划系统」的AI旅游规划师。"
            + "请为用户提供专业、可执行的旅游建议，包括行程安排、景点推荐、美食建议和注意事项。"
            + "回答使用中文，结构清晰，分点表述，控制篇幅，优先结合用户提供的条件。";

    private static final long STREAM_TIMEOUT_SECONDS = 120;

    @Autowired
    private SparkClient sparkClient;

    @Autowired
    private SparkConfig sparkConfig;

    public String chat(String question, List<AiChatMessage> history, String userContext) {
        if (StringUtils.isBlank(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (!isConfigured()) {
            return buildFallbackAnswer(question);
        }

        SparkRequest request = buildRequest(question, history, userContext);
        try {
            SparkSyncChatResponse response = sparkClient.chatSync(request);
            if (response != null && StringUtils.isNotBlank(response.getContent())) {
                return response.getContent();
            }
            return "AI 未返回有效内容，请稍后重试。";
        } catch (SparkException e) {
            throw new RuntimeException("AI 服务调用失败：" + e.getMessage(), e);
        }
    }

    public void chatStream(String question, List<AiChatMessage> history, String userContext,
                           BiConsumer<String, Integer> onChunk) throws Exception {
        if (StringUtils.isBlank(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }
        if (!isConfigured()) {
            onChunk.accept(buildFallbackAnswer(question), 0);
            onChunk.accept("", 2);
            return;
        }

        SparkRequest request = buildRequest(question, history, userContext);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        SparkBaseListener listener = new SparkBaseListener() {
            @Override
            public void onMessage(String content, SparkResponseUsage usage, Integer status,
                                  SparkRequest sparkRequest, SparkResponse sparkResponse, WebSocket webSocket) {
                onChunk.accept(StringUtils.defaultString(content), status == null ? 1 : status);
                if (status != null && status == 2) {
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                if (t instanceof SparkException) {
                    errorRef.set((SparkException) t);
                } else {
                    errorRef.set(new RuntimeException("AI 服务调用失败：" + t.getMessage(), t));
                }
                latch.countDown();
            }
        };

        sparkClient.chatStream(request, listener);

        if (!latch.await(STREAM_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            throw new RuntimeException("AI 响应超时，请稍后重试");
        }
        if (errorRef.get() != null) {
            throw errorRef.get();
        }
    }

    public String generatePlanPrompt(SysTravelPlan plan, List<SysAttractions> attractions) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下条件，生成详细的旅游行程规划：\n");
        sb.append("出发时间：").append(plan.getDepartureDate()).append('\n');
        sb.append("目的地：").append(plan.getDestination()).append('\n');
        sb.append("游玩天数：").append(plan.getDays()).append("天\n");
        sb.append("预算范围：").append(plan.getBudget()).append('\n');
        sb.append("偏好类型：").append(plan.getPreferences()).append('\n');
        sb.append("特殊需求：").append(StringUtils.defaultIfBlank(plan.getSpecialNeeds(), "无")).append('\n');
        if (attractions != null && !attractions.isEmpty()) {
            sb.append("\n系统内可优先考虑的相关景点：\n");
            for (SysAttractions item : attractions) {
                sb.append("- ").append(item.getName());
                if (StringUtils.isNotBlank(item.getLocation())) {
                    sb.append("（").append(item.getLocation()).append("）");
                }
                if (StringUtils.isNotBlank(item.getIntroduce())) {
                    String intro = item.getIntroduce().length() > 80
                            ? item.getIntroduce().substring(0, 80) + "..."
                            : item.getIntroduce();
                    sb.append("：").append(intro);
                }
                sb.append('\n');
            }
        }
        sb.append("\n请按「每日行程 + 景点推荐 + 美食推荐 + 注意事项」输出，每天标题请使用【第X天】格式。");
        return sb.toString();
    }

    private SparkRequest buildRequest(String question, List<AiChatMessage> history, String userContext) {
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.systemContent(
                SYSTEM_PROMPT + (StringUtils.isNotBlank(userContext) ? "\n\n用户信息：" + userContext : "")));

        if (history != null) {
            for (AiChatMessage item : history) {
                if (item == null || StringUtils.isBlank(item.getContent()) || StringUtils.isBlank(item.getRole())) {
                    continue;
                }
                if ("assistant".equals(item.getRole())) {
                    messages.add(SparkMessage.assistantContent(stripHtml(item.getContent())));
                } else {
                    messages.add(SparkMessage.userContent(item.getContent()));
                }
            }
        }
        messages.add(SparkMessage.userContent(question));

        return new SparkRequestBuilder()
                .messages(messages)
                .apiVersion(SparkApiVersion.V3_5)
                .build();
    }

    private boolean isConfigured() {
        return sparkConfig != null
                && StringUtils.isNotBlank(sparkConfig.getAppid())
                && !"your_appid".equalsIgnoreCase(sparkConfig.getAppid())
                && StringUtils.isNotBlank(sparkConfig.getApiKey())
                && !"your_api_key".equalsIgnoreCase(sparkConfig.getApiKey())
                && StringUtils.isNotBlank(sparkConfig.getApiSecret())
                && !"your_api_secret".equalsIgnoreCase(sparkConfig.getApiSecret());
    }

    private String buildFallbackAnswer(String question) {
        return "当前 AI 服务未配置有效密钥，已进入演示模式。\n\n"
                + "关于「" + question + "」的建议：请在 application.yml 中配置 spring.xunfei.client 的 appid、api-key、api-secret 后重启后端。";
    }

    private String stripHtml(String content) {
        return content.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }
}
