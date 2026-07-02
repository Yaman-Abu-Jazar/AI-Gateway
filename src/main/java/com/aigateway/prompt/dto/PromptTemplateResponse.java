package com.aigateway.prompt.dto;

import java.time.Instant;

public record PromptTemplateResponse(
        Long id,
        String name,
        String description,
        String content,
        boolean isPublic,
        Instant createdAt,
        Instant updatedAt
) {}
