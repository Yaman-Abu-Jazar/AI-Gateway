package com.aigateway.ai;

import java.util.List;

public interface AiProvider {

    ChatCompletion complete(ChatRequest request);

    record ChatMessage(String role, String content) {}

    record ChatRequest(String model, List<ChatMessage> messages, Integer maxTokens, Double temperature) {}

    record ChatCompletion(String model, String content, int promptTokens, int completionTokens) {
        public int totalTokens() { return promptTokens + completionTokens; }
    }
}
