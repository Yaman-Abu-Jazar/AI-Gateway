package com.aigateway.apikey.dto;

import java.time.Instant;

public record ApiKeyResponse(
        Long id,
        String name,
        String prefix,
        boolean revoked,
        Instant lastUsedAt,
        Instant createdAt
) {}
