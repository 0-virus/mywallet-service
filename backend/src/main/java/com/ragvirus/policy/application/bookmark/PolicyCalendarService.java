package com.ragvirus.policy.application.bookmark;

import com.ragvirus.policy.api.dto.PolicyCalendarEventResponse;
import com.ragvirus.policy.repository.PolicyCalendarEventRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyCalendarService {

    private final PolicyCalendarEventRepository calendarEventRepository;

    public PolicyCalendarService(PolicyCalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    @Transactional(readOnly = true)
    public List<PolicyCalendarEventResponse> getMonthEvents(Long memberId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return calendarEventRepository.findByMemberIdAndEventDateBetweenOrderByEventDate(memberId, from, to)
                .stream()
                .map(PolicyCalendarEventResponse::from)
                .toList();
    }
}
