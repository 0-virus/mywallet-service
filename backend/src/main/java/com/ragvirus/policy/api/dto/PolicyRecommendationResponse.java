package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.application.eligibility.EligibilityResult;
import com.ragvirus.policy.domain.Policy;
import java.time.LocalDate;
import java.util.List;

public record PolicyRecommendationResponse(
        Long policyId,
        String title,
        String agencyName,
        String regionScope,
        String summary,
        String benefitSummary,
        String applicationStatus,
        String eligibilityStatus,
        LocalDate dueDate,
        Integer dday,
        int score,
        List<String> matchedReasons,
        List<String> needCheckReasons,
        List<String> rejectedReasons,
        String officialUrl
) {
    public static PolicyRecommendationResponse of(Policy policy, EligibilityResult eligibility, int score) {
        return new PolicyRecommendationResponse(
                policy.getId(),
                policy.getTitle(),
                policy.getAgencyName(),
                policy.getRegionScope(),
                policy.getSummary(),
                policy.getBenefitText(),
                policy.getApplicationStatus(),
                eligibility.status(),
                policy.getDueDate(),
                policy.getDday(),
                score,
                eligibility.matchedReasons(),
                eligibility.needCheckReasons(),
                eligibility.rejectedReasons(),
                policy.getOfficialUrl()
        );
    }
}
