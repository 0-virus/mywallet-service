package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyNotificationRepository extends JpaRepository<PolicyNotification, Long> {
}
