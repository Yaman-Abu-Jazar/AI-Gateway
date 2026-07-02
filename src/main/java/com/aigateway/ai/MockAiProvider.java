package com.aigateway.ai;

import java.util.List;

/**
 * Deterministic offline provider used for local dev and tests.
 * Echoes back the last user message so you can wire the full pipeline
 * end-to-end without needing an OpenAI API key.
 */
public class MockAiProvider implements AiProvider {

    @Override
    public ChatCompletion complete(ChatRequest request) {
        List<ChatMessage> messages = request.messages();
        String lastUser = messages.stream()
                .filter(m -> "user".equalsIgnoreCase(m.role()))
                .map(ChatMessage::content)
                .reduce((a, b) -> b)
                .orElse("");
        String reply = "[mock] You said: " + lastUser;
        int promptTokens = messages.stream().mapToInt(m -> tokenEstimate(m.content())).sum();
        int completionTokens = tokenEstimate(reply);
        return new ChatCompletion(
                request.model() == null ? "mock-echo" : request.model(),
                reply, promptTokens, completionTokens);
    }

    private static int tokenEstimate(String text) {
        if (text == null || text.isEmpty()) return 0;
        return Math.max(1, text.length() / 4);
    }
}
