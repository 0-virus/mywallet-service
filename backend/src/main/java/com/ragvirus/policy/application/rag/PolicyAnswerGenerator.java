package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import java.util.List;

public interface PolicyAnswerGenerator {

    String generate(
            String message,
            List<PolicyRecommendationResponse> recommendations,
            List<RetrievedPolicyChunk> retrievedChunks
    );
}
