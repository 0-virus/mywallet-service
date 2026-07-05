package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyRecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRecommendationLogRepository extends JpaRepository<PolicyRecommendationLog, Long> {
}
