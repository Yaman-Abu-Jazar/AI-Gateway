package com.aigateway.ai;

import com.aigateway.common.BadRequestException;
import com.aigateway.config.AppProperties;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls any OpenAI-compatible {@code /chat/completions} endpoint.
 * Works with OpenAI, Groq, Together, OpenRouter, Ollama (with the OpenAI adapter), etc.
 */
@Slf4j
public class OpenAiProvider implements AiProvider {

    private final RestClient client;
    private final String defaultModel;

    public OpenAiProvider(AppProperties.OpenAi cfg) {
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY not configured. Set app.ai.openai.api-key or switch app.ai.provider=mock.");
        }
        this.defaultModel = cfg.getDefaultModel();
        this.client = RestClient.builder()
                .baseUrl(cfg.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ChatCompletion complete(ChatRequest request) {
        String model = request.model() != null ? request.model() : defaultModel;
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", request.messages().stream()
                        .map(m -> Map.of("role", m.role(), "content", m.content()))
                        .toList(),
                "max_tokens", request.maxTokens() != null ? request.maxTokens() : 1024,
                "temperature", request.temperature() != null ? request.temperature() : 0.7
        );

        try {
            OpenAiResponse response = client.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(OpenAiResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new BadRequestException("Empty response from upstream AI provider");
            }
            String content = response.choices().get(0).message().content();
            int prompt = response.usage() != null ? response.usage().promptTokens() : 0;
            int completion = response.usage() != null ? response.usage().completionTokens() : 0;
            return new ChatCompletion(model, content, prompt, completion);
        } catch (RestClientResponseException e) {
            log.warn("Upstream AI error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BadRequestException("Upstream AI error: " + e.getStatusCode());
        }
    }

    private record OpenAiResponse(List<Choice> choices, Usage usage) {
        private record Choice(Msg message) {}
        private record Msg(String role, String content) {}
        private record Usage(
                @com.fasterxml.jackson.annotation.JsonProperty("prompt_tokens") int promptTokens,
                @com.fasterxml.jackson.annotation.JsonProperty("completion_tokens") int completionTokens,
                @com.fasterxml.jackson.annotation.JsonProperty("total_tokens") int totalTokens
        ) {}
    }
}
