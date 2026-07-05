package com.ragvirus.policy.application.rag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyDocumentChunk;
import com.ragvirus.policy.domain.PolicyEmbedding;
import com.ragvirus.policy.repository.PolicyDocumentChunkRepository;
import com.ragvirus.policy.repository.PolicyEmbeddingRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyEmbeddingService {

    private static final TypeReference<List<Double>> DOUBLE_LIST = new TypeReference<>() {
    };

    private final PolicyDocumentChunkRepository chunkRepository;
    private final PolicyEmbeddingRepository embeddingRepository;
    private final TextEmbeddingProvider embeddingProvider;
    private final ObjectMapper objectMapper;

    public PolicyEmbeddingService(
            PolicyDocumentChunkRepository chunkRepository,
            PolicyEmbeddingRepository embeddingRepository,
            TextEmbeddingProvider embeddingProvider,
            ObjectMapper objectMapper
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingRepository = embeddingRepository;
        this.embeddingProvider = embeddingProvider;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void rebuildEmbeddings(Policy policy) {
        embeddingRepository.deleteByChunk_Policy_Id(policy.getId());
        List<PolicyDocumentChunk> chunks = chunkRepository.findByPolicy_Id(policy.getId());
        embeddingRepository.saveAll(chunks.stream()
                .map(this::embedChunk)
                .toList());
    }

    public String currentModelName() {
        return embeddingProvider.modelName();
    }

    public double[] embedQuery(String text) {
        return embeddingProvider.embed(text);
    }

    public double[] parseEmbedding(String embeddingJson) {
        try {
            List<Double> values = objectMapper.readValue(embeddingJson, DOUBLE_LIST);
            double[] vector = new double[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = values.get(i);
            }
            return vector;
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid embedding JSON", ex);
        }
    }

    private PolicyEmbedding embedChunk(PolicyDocumentChunk chunk) {
        return new PolicyEmbedding(chunk, toJson(embeddingProvider.embed(chunk.getContent())), embeddingProvider.modelName());
    }

    private String toJson(double[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to serialize embedding vector", ex);
        }
    }
}
