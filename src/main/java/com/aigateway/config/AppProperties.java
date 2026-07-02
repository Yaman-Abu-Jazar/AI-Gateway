package com.aigateway.config;

import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Security security = new Security();
    private Ai ai = new Ai();
    private Map<String, PlanLimits> plans = Map.of();

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();
    }

    @Data
    public static class Jwt {
        private String secret;
        private int expirationMinutes = 60;
        private String issuer = "ai-gateway";
    }

    @Data
    public static class Ai {
        private String provider = "mock";
        private OpenAi openai = new OpenAi();
    }

    @Data
    public static class OpenAi {
        private String baseUrl;
        private String apiKey;
        private String defaultModel;
        private int timeoutSeconds = 60;
    }

    @Data
    public static class PlanLimits {
        private int requestsPerMinute;
        private int requestsPerDay;
        private int maxTokensPerRequest;
    }
}
