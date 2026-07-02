package com.aigateway.conversation.dto;

import java.time.Instant;

public record ConversationSummary(
        Long id,
        String title,
        String model,
        Instant createdAt,
        Instant updatedAt
) {}
