package com.aigateway.ai;

import com.aigateway.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiProviderConfig {

    @Bean
    public AiProvider aiProvider(AppProperties props) {
        String provider = props.getAi().getProvider();
        log.info("Configuring AI provider: {}", provider);
        return switch (provider == null ? "mock" : provider.toLowerCase()) {
            case "openai" -> new OpenAiProvider(props.getAi().getOpenai());
            case "mock" -> new MockAiProvider();
            default -> throw new IllegalStateException("Unknown app.ai.provider: " + provider);
        };
    }
}
