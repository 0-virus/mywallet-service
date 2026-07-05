package com.ragvirus.policy.application.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.ragvirus.policy.config.PolicyOpenAiProperties;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "policy.rag.embedding-provider", havingValue = "openai")
public class OpenAiEmbeddingProvider implements TextEmbeddingProvider {

    private final PolicyOpenAiProperties properties;
    private final RestClient restClient;

    public OpenAiEmbeddingProvider(PolicyOpenAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public String modelName() {
        return properties.embeddingModel();
    }

    @Override
    public double[] embed(String text) {
        if (!properties.hasApiKey()) {
            throw new IllegalStateException("OPENAI_API_KEY is required when POLICY_RAG_EMBEDDING_PROVIDER=openai");
        }

        JsonNode response = restClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + properties.apiKey())
                .body(Map.of(
                        "model", properties.embeddingModel(),
                        "input", text == null ? "" : text
                ))
                .retrieve()
                .body(JsonNode.class);

        JsonNode embedding = response.path("data").path(0).path("embedding");
        if (!embedding.isArray()) {
            throw new IllegalStateException("OpenAI embedding response did not include data[0].embedding");
        }

        double[] vector = new double[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).asDouble();
        }
        return vector;
    }
}
