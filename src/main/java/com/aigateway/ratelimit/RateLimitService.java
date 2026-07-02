package com.aigateway.ratelimit;

import com.aigateway.common.RateLimitExceededException;
import com.aigateway.config.AppProperties;
import com.aigateway.config.AppProperties.PlanLimits;
import com.aigateway.user.User;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Per-user, in-memory rate limiter using Bucket4j. Two windows are enforced:
 * requests-per-minute and requests-per-day, driven by the user's plan.
 *
 * <p>For a distributed deployment, swap the in-memory buckets for a Redis-backed
 * {@code LettuceBasedProxyManager} without touching callers.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final AppProperties props;
    private final ConcurrentMap<Long, Bucket> minuteBuckets = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Bucket> dayBuckets = new ConcurrentHashMap<>();

    public void checkAndConsume(User user) {
        PlanLimits limits = limitsFor(user);
        Bucket minute = minuteBuckets.computeIfAbsent(user.getId(), id -> Bucket.builder()
                .addLimit(Bandwidth.simple(limits.getRequestsPerMinute(), Duration.ofMinutes(1)))
                .build());
        Bucket day = dayBuckets.computeIfAbsent(user.getId(), id -> Bucket.builder()
                .addLimit(Bandwidth.simple(limits.getRequestsPerDay(), Duration.ofDays(1)))
                .build());

        ConsumptionProbe minuteProbe = minute.tryConsumeAndReturnRemaining(1);
        if (!minuteProbe.isConsumed()) {
            long retrySec = Math.max(1, minuteProbe.getNanosToWaitForRefill() / 1_000_000_000L);
            throw new RateLimitExceededException(
                    "Per-minute limit reached for plan " + user.getPlan() + ". Retry after " + retrySec + "s");
        }
        ConsumptionProbe dayProbe = day.tryConsumeAndReturnRemaining(1);
        if (!dayProbe.isConsumed()) {
            throw new RateLimitExceededException(
                    "Daily limit reached for plan " + user.getPlan() + ". Upgrade to increase quota.");
        }
    }

    public PlanLimits limitsFor(User user) {
        String key = user.getPlan().name().toLowerCase(Locale.ROOT);
        PlanLimits limits = props.getPlans().get(key);
        if (limits == null) {
            throw new IllegalStateException("No plan limits configured for " + user.getPlan());
        }
        return limits;
    }
}
