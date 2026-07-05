package com.ragvirus.policy.application.normalization;

import java.time.LocalDate;

public record NormalizedPolicy(
        String sourceType,
        String sourcePolicyId,
        String title,
        String summary,
        String agencyName,
        String departmentName,
        String category,
        String supportType,
        String targetText,
        String criteriaText,
        String benefitText,
        String applyText,
        String requiredDocsText,
        String contactText,
        String regionScope,
        String officialUrl,
        String applicationUrl,
        LocalDate startDate,
        LocalDate dueDate,
        boolean alwaysOpen
) {
}
