package com.aigateway.security;

import com.aigateway.apikey.ApiKey;
import com.aigateway.apikey.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Accepts an API key from the `X-API-Key` header (or "Authorization: ApiKey <key>").
 * Programmatic clients use this instead of a short-lived JWT.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyHasher hasher;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String raw = request.getHeader(HEADER);
        if (!StringUtils.hasText(raw)) {
            String authz = request.getHeader("Authorization");
            if (StringUtils.hasText(authz) && authz.startsWith("ApiKey ")) {
                raw = authz.substring("ApiKey ".length()).trim();
            }
        }

        if (StringUtils.hasText(raw)) {
            String hash = hasher.hash(raw);
            apiKeyRepository.findByKeyHashWithUser(hash).ifPresent(apiKey -> {
                if (apiKey.isRevoked() || !apiKey.getUser().isEnabled()) return;
                apiKey.setLastUsedAt(Instant.now());
                apiKeyRepository.save(apiKey);

                AppUserDetails principal = new AppUserDetails(apiKey.getUser());
                var auth = new UsernamePasswordAuthenticationToken(
                        principal, apiKey, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.setAttribute("apiKey", apiKey);
            });
        }

        chain.doFilter(request, response);
    }

    public static ApiKey currentApiKey(HttpServletRequest req) {
        Object v = req.getAttribute("apiKey");
        return v instanceof ApiKey ak ? ak : null;
    }
}
