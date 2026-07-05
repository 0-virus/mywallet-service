package com.ragvirus.policy.api.dto;

import java.time.LocalDate;

public record UserPolicyProfileRequest(
        LocalDate birthDate,
        Integer age,
        String incomeRange,
        String employmentStatus,
        Boolean studentStatus,
        String householdStatus,
        String housingStatus,
        String interestCategories,
        boolean notificationAgreed
) {
}
