package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.api.dto.PolicyChatRequest;
import com.ragvirus.policy.api.dto.PolicyChatResponse;
import com.ragvirus.policy.api.dto.PolicyChatCitationResponse;
import com.ragvirus.policy.api.dto.PolicyRecommendationRequest;
import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.application.PolicyRecommendationService;
import com.ragvirus.policy.application.eligibility.UserPolicyCondition;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PolicyChatService {

    private final PolicyRecommendationService recommendationService;
    private final PolicyRetriever policyRetriever;
    private final PolicyAnswerGenerator answerGenerator;

    public PolicyChatService(
            PolicyRecommendationService recommendationService,
            PolicyRetriever policyRetriever,
            PolicyAnswerGenerator answerGenerator
    ) {
        this.recommendationService = recommendationService;
        this.policyRetriever = policyRetriever;
        this.answerGenerator = answerGenerator;
    }

    public PolicyChatResponse answer(PolicyChatRequest request) {
        PolicyRecommendationRequest profile = request.profile() == null
                ? new PolicyRecommendationRequest(null, null, null, null, null, null, null, null)
                : request.profile();

        List<PolicyRecommendationResponse> recommendations = recommendationService.recommend(new UserPolicyCondition(
                profile.age(),
                profile.regionId(),
                profile.incomeRange(),
                profile.employmentStatus(),
                profile.studentStatus(),
                profile.householdStatus(),
                profile.housingStatus(),
                profile.interestCategories()
        ));

        List<RetrievedPolicyChunk> retrievedChunks = policyRetriever.retrieve(request.message(), 5);
        List<PolicyRecommendationResponse> rankedRecommendations = rankByRetrieval(recommendations, retrievedChunks);
        List<PolicyChatCitationResponse> citations = retrievedChunks.stream()
                .map(PolicyChatCitationResponse::from)
                .toList();

        return new PolicyChatResponse(
                answerGenerator.generate(request.message(), rankedRecommendations, retrievedChunks),
                rankedRecommendations,
                citations
        );
    }

    private List<PolicyRecommendationResponse> rankByRetrieval(
            List<PolicyRecommendationResponse> recommendations,
            List<RetrievedPolicyChunk> retrievedChunks
    ) {
        if (retrievedChunks.isEmpty()) {
            return recommendations;
        }
        Set<Long> retrievedPolicyIds = retrievedChunks.stream()
                .map(RetrievedPolicyChunk::policyId)
                .collect(java.util.stream.Collectors.toSet());
        return recommendations.stream()
                .sorted(Comparator
                        .comparing((PolicyRecommendationResponse response) -> retrievedPolicyIds.contains(response.policyId())).reversed()
                        .thenComparingInt(PolicyRecommendationResponse::score).reversed())
                .toList();
    }
}
