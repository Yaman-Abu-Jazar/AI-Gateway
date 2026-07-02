package com.aigateway.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptTemplateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank String content,
        boolean isPublic
) {}
