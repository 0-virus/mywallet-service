package com.ragvirus.policy.application.condition;

public record ExtractedPolicyCondition(
        Integer minAge,
        Integer maxAge,
        String gender,
        String incomeBand,
        String employmentStatus,
        Boolean studentStatus,
        String householdStatus,
        String housingStatus,
        String businessStatus,
        String conditionSummary,
        boolean needManualCheck,
        String missingFields,
        String conditionSource
) {
}
