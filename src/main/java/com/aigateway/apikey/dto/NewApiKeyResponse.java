package com.aigateway.apikey.dto;

/**
 * Returned only on creation. The plaintext {@code key} is never shown again — the client must save it.
 */
public record NewApiKeyResponse(
        ApiKeyResponse metadata,
        String key
) {}
