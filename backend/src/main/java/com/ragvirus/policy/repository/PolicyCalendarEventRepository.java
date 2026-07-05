package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyCalendarEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyCalendarEventRepository extends JpaRepository<PolicyCalendarEvent, Long> {

    List<PolicyCalendarEvent> findByMemberIdAndEventDateBetweenOrderByEventDate(Long memberId, LocalDate from, LocalDate to);

    void deleteByMemberIdAndPolicy_Id(Long memberId, Long policyId);
}
