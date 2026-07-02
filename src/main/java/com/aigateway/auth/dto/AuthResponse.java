package com.aigateway.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMinutes,
        UserSummary user
) {
    public record UserSummary(Long id, String email, String displayName, String plan, String role) {}
}
