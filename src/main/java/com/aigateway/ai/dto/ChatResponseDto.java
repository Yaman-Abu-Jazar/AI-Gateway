package com.aigateway.ai.dto;

public record ChatResponseDto(
        Long conversationId,
        Long messageId,
        String model,
        String content,
        int promptTokens,
        int completionTokens,
        int totalTokens,
        long latencyMs
) {}
