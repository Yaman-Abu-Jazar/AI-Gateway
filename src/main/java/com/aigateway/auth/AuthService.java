package com.aigateway.auth;

import com.aigateway.auth.dto.AuthResponse;
import com.aigateway.auth.dto.LoginRequest;
import com.aigateway.auth.dto.RegisterRequest;
import com.aigateway.common.BadRequestException;
import com.aigateway.config.AppProperties;
import com.aigateway.security.JwtService;
import com.aigateway.user.Plan;
import com.aigateway.user.Role;
import com.aigateway.user.User;
import com.aigateway.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties props;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("Email already registered");
        }
        User user = User.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName())
                .plan(Plan.FREE)
                .role(Role.USER)
                .enabled(true)
                .build();
        user = userRepository.save(user);
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isEnabled()) throw new BadCredentialsException("Account disabled");
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtService.generateToken(user);
        AuthResponse.UserSummary summary = new AuthResponse.UserSummary(
                user.getId(), user.getEmail(), user.getDisplayName(),
                user.getPlan().name(), user.getRole().name());
        return new AuthResponse(token, "Bearer",
                props.getSecurity().getJwt().getExpirationMinutes(), summary);
    }
}
