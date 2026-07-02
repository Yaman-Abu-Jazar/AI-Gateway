package com.aigateway.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequestDto(
        @NotBlank @Size(max = 32000) String message,
        Long conversationId,
        String model,
        Long promptTemplateId,
        Integer maxTokens,
        Double temperature
) {}
