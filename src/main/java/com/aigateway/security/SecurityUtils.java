package com.aigateway.security;

import com.aigateway.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        if (auth.getPrincipal() instanceof AppUserDetails details) {
            return details.getUser();
        }
        throw new AccessDeniedException("Unexpected principal type");
    }
}
