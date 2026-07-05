package com.ragvirus.policy.api.dto;

public record PolicyRecommendationRequest(
        Integer age,
        Long regionId,
        String incomeRange,
        String employmentStatus,
        Boolean studentStatus,
        String householdStatus,
        String housingStatus,
        String interestCategories
) {
}
