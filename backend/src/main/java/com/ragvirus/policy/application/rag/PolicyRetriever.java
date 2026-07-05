package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.domain.PolicyEmbedding;
import com.ragvirus.policy.repository.PolicyEmbeddingRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyRetriever {

    private static final double MIN_SIMILARITY = 0.30d;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[가-힣A-Za-z0-9]+");
    private static final Set<String> STOPWORDS = Set.of(
            "서울", "사는", "살", "인데", "받을", "수", "있어", "지원", "정책", "추천", "청년", "취준생"
    );

    private final PolicyEmbeddingRepository embeddingRepository;
    private final PolicyEmbeddingService embeddingService;

    public PolicyRetriever(
            PolicyEmbeddingRepository embeddingRepository,
            PolicyEmbeddingService embeddingService
    ) {
        this.embeddingRepository = embeddingRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional(readOnly = true)
    public List<RetrievedPolicyChunk> retrieve(String query, int topK) {
        double[] queryEmbedding = embeddingService.embedQuery(query);
        List<String> queryTokens = queryTokens(query);
        Map<Long, RetrievedPolicyChunk> topChunkByPolicy = new LinkedHashMap<>();

        embeddingRepository.findByEmbeddingModel(embeddingService.currentModelName()).stream()
                .map(embedding -> toRetrievedChunk(embedding, queryEmbedding, queryTokens))
                .filter(result -> result.similarityScore() >= MIN_SIMILARITY)
                .sorted(Comparator.comparingDouble(RetrievedPolicyChunk::similarityScore).reversed())
                .forEach(result -> topChunkByPolicy.putIfAbsent(result.policyId(), result));

        return topChunkByPolicy.values().stream()
                .limit(topK)
                .toList();
    }

    private RetrievedPolicyChunk toRetrievedChunk(PolicyEmbedding embedding, double[] queryEmbedding, List<String> queryTokens) {
        double[] chunkEmbedding = embeddingService.parseEmbedding(embedding.getEmbeddingJson());
        double vectorScore = VectorMath.cosine(queryEmbedding, chunkEmbedding);
        double lexicalScore = lexicalScore(queryTokens, embedding.getChunk().getContent());
        double score = (vectorScore * 0.55d) + (lexicalScore * 0.45d);
        return RetrievedPolicyChunk.of(embedding.getChunk(), score);
    }

    private double lexicalScore(List<String> queryTokens, String content) {
        if (queryTokens.isEmpty() || content == null || content.isBlank()) {
            return 0.0d;
        }
        int matches = 0;
        for (String token : queryTokens) {
            if (content.contains(token)) {
                matches++;
            }
        }
        return (double) matches / queryTokens.size();
    }

    private List<String> queryTokens(String query) {
        Matcher matcher = TOKEN_PATTERN.matcher(query == null ? "" : query);
        return matcher.results()
                .map(match -> match.group().trim())
                .filter(token -> token.length() >= 2)
                .filter(token -> !STOPWORDS.contains(token))
                .distinct()
                .toList();
    }
}
