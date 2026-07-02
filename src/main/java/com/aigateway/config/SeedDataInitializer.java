package com.aigateway.config;

import com.aigateway.user.Plan;
import com.aigateway.user.Role;
import com.aigateway.user.User;
import com.aigateway.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SeedDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("admin@aigateway.local")) return;

        User admin = User.builder()
                .email("admin@aigateway.local")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .displayName("Admin")
                .role(Role.ADMIN)
                .plan(Plan.ENTERPRISE)
                .enabled(true)
                .build();
        userRepository.save(admin);

        User demo = User.builder()
                .email("demo@aigateway.local")
                .passwordHash(passwordEncoder.encode("Demo1234!"))
                .displayName("Demo User")
                .role(Role.USER)
                .plan(Plan.FREE)
                .enabled(true)
                .build();
        userRepository.save(demo);

        log.info("Seeded dev users: admin@aigateway.local / Admin123!  |  demo@aigateway.local / Demo1234!");
    }
}
