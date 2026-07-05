package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.domain.PolicyBookmark;
import java.time.LocalDate;

public record PolicyBookmarkResponse(
        Long bookmarkId,
        Long policyId,
        String title,
        String regionScope,
        String applicationStatus,
        LocalDate startDate,
        LocalDate dueDate,
        Integer dday,
        String applyStatus,
        boolean notificationEnabled
) {
    public static PolicyBookmarkResponse from(PolicyBookmark bookmark) {
        return new PolicyBookmarkResponse(
                bookmark.getId(),
                bookmark.getPolicy().getId(),
                bookmark.getPolicy().getTitle(),
                bookmark.getPolicy().getRegionScope(),
                bookmark.getPolicy().getApplicationStatus(),
                bookmark.getPolicy().getStartDate(),
                bookmark.getPolicy().getDueDate(),
                bookmark.getPolicy().getDday(),
                bookmark.getApplyStatus(),
                bookmark.isNotificationEnabled()
        );
    }
}
