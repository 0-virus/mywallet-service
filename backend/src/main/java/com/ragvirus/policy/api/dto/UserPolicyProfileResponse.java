package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.domain.UserPolicyProfile;
import java.time.Instant;
import java.time.LocalDate;

public record UserPolicyProfileResponse(
        Long id,
        Long memberId,
        LocalDate birthDate,
        Integer age,
        String incomeRange,
        String employmentStatus,
        Boolean studentStatus,
        String householdStatus,
        String housingStatus,
        String interestCategories,
        boolean notificationAgreed,
        Instant updatedAt
) {

    public static UserPolicyProfileResponse from(UserPolicyProfile profile) {
        return new UserPolicyProfileResponse(
                profile.getId(),
                profile.getMemberId(),
                profile.getBirthDate(),
                profile.getAge(),
                profile.getIncomeRange(),
                profile.getEmploymentStatus(),
                profile.getStudentStatus(),
                profile.getHouseholdStatus(),
                profile.getHousingStatus(),
                profile.getInterestCategories(),
                profile.isNotificationAgreed(),
                profile.getUpdatedAt()
        );
    }
}
