package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.PolicyCalendarEventResponse;
import com.ragvirus.policy.application.bookmark.PolicyCalendarService;
import java.time.YearMonth;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policy-calendar")
public class PolicyCalendarController {

    private final PolicyCalendarService calendarService;

    public PolicyCalendarController(PolicyCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public List<PolicyCalendarEventResponse> monthEvents(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestParam YearMonth yearMonth
    ) {
        return calendarService.getMonthEvents(memberId, yearMonth);
    }
}
