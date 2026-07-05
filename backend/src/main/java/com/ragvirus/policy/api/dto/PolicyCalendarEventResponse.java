package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.domain.PolicyCalendarEvent;
import java.time.LocalDate;

public record PolicyCalendarEventResponse(
        Long eventId,
        Long policyId,
        String policyTitle,
        String eventType,
        LocalDate eventDate,
        String title
) {
    public static PolicyCalendarEventResponse from(PolicyCalendarEvent event) {
        return new PolicyCalendarEventResponse(
                event.getId(),
                event.getPolicy().getId(),
                event.getPolicy().getTitle(),
                event.getEventType(),
                event.getEventDate(),
                event.getTitle()
        );
    }
}
