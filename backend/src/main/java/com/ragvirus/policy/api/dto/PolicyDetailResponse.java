package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.domain.Policy;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PolicyDetailResponse(
        Long policyId,
        String sourceType,
        String sourcePolicyId,
        String title,
        String agencyName,
        String departmentName,
        String regionScope,
        String category,
        String supportType,
        String summary,
        String targetText,
        String criteriaText,
        String benefitText,
        String applyText,
        String requiredDocsText,
        String contactText,
        String applicationStatus,
        String eligibilityStatus,
        LocalDate startDate,
        LocalDate dueDate,
        Integer dday,
        String officialUrl,
        String applicationUrl,
        List<String> matchedReasons,
        List<String> needCheckReasons,
        List<String> rejectedReasons,
        Instant lastSyncedAt
) {
    public static PolicyDetailResponse from(Policy policy) {
        return new PolicyDetailResponse(
                policy.getId(),
                policy.getSourceType(),
                policy.getSourcePolicyId(),
                policy.getTitle(),
                policy.getAgencyName(),
                policy.getDepartmentName(),
                policy.getRegionScope(),
                policy.getCategory(),
                policy.getSupportType(),
                policy.getSummary(),
                policy.getTargetText(),
                policy.getCriteriaText(),
                policy.getBenefitText(),
                policy.getApplyText(),
                policy.getRequiredDocsText(),
                policy.getContactText(),
                policy.getApplicationStatus(),
                "need_check",
                policy.getStartDate(),
                policy.getDueDate(),
                policy.getDday(),
                policy.getOfficialUrl(),
                policy.getApplicationUrl(),
                List.of(),
                List.of("신청 가능 여부 판별은 사용자 조건 입력 후 계산됩니다."),
                List.of(),
                policy.getLastSyncedAt()
        );
    }
}
