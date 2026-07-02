package com.aigateway.conversation.dto;

import java.time.Instant;
import java.util.List;

public record ConversationDetail(
        Long id,
        String title,
        String model,
        String systemPrompt,
        Instant createdAt,
        Instant updatedAt,
        List<MessageView> messages
) {
    public record MessageView(Long id, String role, String content,
                              int promptTokens, int completionTokens, Instant createdAt) {}
}
