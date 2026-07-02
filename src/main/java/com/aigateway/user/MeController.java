package com.aigateway.user;

import com.aigateway.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Me", description = "Current user profile")
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    public record MeResponse(Long id, String email, String displayName, String plan, String role) {}

    @Operation(summary = "Get the authenticated user's profile")
    @GetMapping
    public MeResponse me() {
        User u = SecurityUtils.currentUser();
        return new MeResponse(u.getId(), u.getEmail(), u.getDisplayName(),
                u.getPlan().name(), u.getRole().name());
    }
}
