package com.project.travel.domain;

import lombok.Data;

import java.util.List;

/**
 * AI 对话请求
 */
@Data
public class AiChatRequest {

    private String question;

    private List<AiChatMessage> history;
}
