package com.ragvirus.policy.application.eligibility;

public record UserPolicyCondition(
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
