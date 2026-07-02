package com.aigateway.usage;

import com.aigateway.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Usage", description = "Inspect the caller's own usage stats")
@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageRepository usageRepository;

    public record UsageSummary(
            long requestsLast24h,
            long tokensLast24h,
            long requestsLast30d,
            long tokensLast30d
    ) {}

    @Operation(summary = "Get a rolling summary of the current user's usage")
    @GetMapping("/summary")
    public UsageSummary summary() {
        var user = SecurityUtils.currentUser();
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant last30d = Instant.now().minus(30, ChronoUnit.DAYS);
        return new UsageSummary(
                usageRepository.countRequestsSince(user, last24h),
                usageRepository.sumTokensSince(user, last24h),
                usageRepository.countRequestsSince(user, last30d),
                usageRepository.sumTokensSince(user, last30d)
        );
    }
}
