package com.project.travel.domain;

import lombok.Data;

/**
 * AI 对话消息
 */
@Data
public class AiChatMessage {

    private String role;

    private String content;
}
