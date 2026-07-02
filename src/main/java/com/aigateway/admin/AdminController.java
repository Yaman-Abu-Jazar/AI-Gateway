package com.aigateway.admin;

import com.aigateway.common.NotFoundException;
import com.aigateway.user.Plan;
import com.aigateway.user.User;
import com.aigateway.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "Administrative endpoints (ADMIN role required)")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    public record UserRow(Long id, String email, String displayName, String plan, String role, boolean enabled) {}

    public record UpdatePlanRequest(Plan plan) {}

    public record UpdateEnabledRequest(boolean enabled) {}

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public List<UserRow> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserRow(u.getId(), u.getEmail(), u.getDisplayName(),
                        u.getPlan().name(), u.getRole().name(), u.isEnabled()))
                .toList();
    }

    @Operation(summary = "Change a user's plan (FREE / PRO / ENTERPRISE)")
    @PutMapping("/users/{id}/plan")
    public UserRow setPlan(@PathVariable Long id, @RequestBody UpdatePlanRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        u.setPlan(req.plan());
        userRepository.save(u);
        return new UserRow(u.getId(), u.getEmail(), u.getDisplayName(),
                u.getPlan().name(), u.getRole().name(), u.isEnabled());
    }

    @Operation(summary = "Enable or disable a user account")
    @PutMapping("/users/{id}/enabled")
    public UserRow setEnabled(@PathVariable Long id, @RequestBody UpdateEnabledRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        u.setEnabled(req.enabled());
        userRepository.save(u);
        return new UserRow(u.getId(), u.getEmail(), u.getDisplayName(),
                u.getPlan().name(), u.getRole().name(), u.isEnabled());
    }
}
