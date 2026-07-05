package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyCondition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyConditionRepository extends JpaRepository<PolicyCondition, Long> {

    List<PolicyCondition> findByPolicy_Id(Long policyId);

    void deleteByPolicy_Id(Long policyId);
}
