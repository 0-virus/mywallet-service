package com.ragvirus.policy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "policy.rag.openai")
public record PolicyOpenAiProperties(
        String apiKey,
        String baseUrl,
        String embeddingModel,
        String chatModel
) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
