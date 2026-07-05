package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.domain.Policy;
import java.time.LocalDate;

public record PolicySummaryResponse(
        Long policyId,
        String title,
        String agencyName,
        String regionScope,
        String category,
        String summary,
        String benefitSummary,
        String applicationStatus,
        String eligibilityStatus,
        LocalDate startDate,
        LocalDate dueDate,
        Integer dday,
        String applyMethod,
        String officialUrl,
        boolean bookmarked
) {
    public static PolicySummaryResponse from(Policy policy) {
        return new PolicySummaryResponse(
                policy.getId(),
                policy.getTitle(),
                policy.getAgencyName(),
                policy.getRegionScope(),
                policy.getCategory(),
                policy.getSummary(),
                policy.getBenefitText(),
                policy.getApplicationStatus(),
                "need_check",
                policy.getStartDate(),
                policy.getDueDate(),
                policy.getDday(),
                policy.getApplyText(),
                policy.getOfficialUrl(),
                false
        );
    }
}
