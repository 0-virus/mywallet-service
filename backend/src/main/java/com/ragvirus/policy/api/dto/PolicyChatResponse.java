package com.ragvirus.policy.api.dto;

import java.util.List;

public record PolicyChatResponse(
        String answer,
        List<PolicyRecommendationResponse> recommendations,
        List<PolicyChatCitationResponse> citations
) {
}
