package com.ragvirus.policy.api.dto;

public record PolicyChatRequest(
        String message,
        PolicyRecommendationRequest profile
) {
}
