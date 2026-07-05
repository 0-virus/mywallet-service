package com.ragvirus.policy.application.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.config.PolicyOpenAiProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "policy.rag.chat-provider", havingValue = "openai")
public class OpenAiPolicyAnswerGenerator implements PolicyAnswerGenerator {

    private final PolicyOpenAiProperties properties;
    private final RestClient restClient;

    public OpenAiPolicyAnswerGenerator(PolicyOpenAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public String generate(
            String message,
            List<PolicyRecommendationResponse> recommendations,
            List<RetrievedPolicyChunk> retrievedChunks
    ) {
        if (!properties.hasApiKey()) {
            throw new IllegalStateException("OPENAI_API_KEY is required when POLICY_RAG_CHAT_PROVIDER=openai");
        }

        JsonNode response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + properties.apiKey())
                .body(Map.of(
                        "model", properties.chatModel(),
                        "temperature", 0.2,
                        "messages", List.of(
                                Map.of("role", "system", "content", systemPrompt()),
                                Map.of("role", "user", "content", buildUserPrompt(message, recommendations, retrievedChunks))
                        )
                ))
                .retrieve()
                .body(JsonNode.class);

        String content = response.path("choices").path(0).path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("OpenAI chat response did not include choices[0].message.content");
        }
        return content.trim();
    }

    private String systemPrompt() {
        return """
                너는 청년 정책 추천 서비스의 상담 챗봇이다.
                반드시 제공된 정책 추천 결과와 근거 chunk만 사용해 한국어로 답한다.
                신청 가능 여부를 단정하지 말고 '신청 가능성이 높음', '확인 필요', '신청 불가'를 구분한다.
                확인 필요 조건, 신청기한, 신청방법, 공식 링크 부재 여부를 숨기지 않는다.
                검색 근거 chunk의 정책과 추천 결과의 정책이 다르면 서로 다른 정책이라고 명확히 구분한다.
                특정 정책이 월세/저축/취업 등 사용자가 물은 분야를 지원한다고 말하려면 근거 chunk에 그 분야가 직접 포함되어 있어야 한다.
                추천 결과에 있는 정책이라도 사용자 질문 분야와 직접 관련된 근거가 없으면 "조건상 함께 확인할 후보"라고만 말한다.
                정책 원문에 없는 정보는 추측하지 말고 추가 확인이 필요하다고 말한다.
                답변은 4~6문장으로 간결하게 작성한다.
                """;
    }

    private String buildUserPrompt(
            String message,
            List<PolicyRecommendationResponse> recommendations,
            List<RetrievedPolicyChunk> retrievedChunks
    ) {
        return """
                [사용자 질문]
                %s

                [추천 결과]
                %s

                [검색 근거 chunk]
                %s

                위 자료만 근거로 사용자에게 추천 답변을 작성해라.
                """.formatted(
                nullToBlank(message),
                formatRecommendations(recommendations),
                formatChunks(retrievedChunks)
        );
    }

    private String formatRecommendations(List<PolicyRecommendationResponse> recommendations) {
        if (recommendations.isEmpty()) {
            return "추천 결과 없음";
        }
        List<String> lines = new ArrayList<>();
        for (PolicyRecommendationResponse recommendation : recommendations.stream().limit(3).toList()) {
            lines.add("""
                    - 정책명: %s
                      기관: %s
                      신청상태: %s
                      판별상태: %s
                      점수: %d
                      충족 사유: %s
                      확인 필요: %s
                      공식 링크: %s
                    """.formatted(
                    recommendation.title(),
                    nullToBlank(recommendation.agencyName()),
                    recommendation.applicationStatus(),
                    recommendation.eligibilityStatus(),
                    recommendation.score(),
                    recommendation.matchedReasons(),
                    recommendation.needCheckReasons(),
                    nullToBlank(recommendation.officialUrl())
            ));
        }
        return String.join("\n", lines);
    }

    private String formatChunks(List<RetrievedPolicyChunk> chunks) {
        if (chunks.isEmpty()) {
            return "검색 근거 없음";
        }
        List<String> lines = new ArrayList<>();
        for (RetrievedPolicyChunk chunk : chunks.stream().limit(5).toList()) {
            lines.add("""
                    - 정책명: %s
                      chunk 유형: %s
                      유사도: %.3f
                      원문:
                      %s
                    """.formatted(
                    chunk.policyTitle(),
                    chunk.chunkType(),
                    chunk.similarityScore(),
                    trim(chunk.content())
            ));
        }
        return String.join("\n", lines);
    }

    private String trim(String value) {
        String normalized = nullToBlank(value).replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 1200) {
            return normalized;
        }
        return normalized.substring(0, 1200) + "...";
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
