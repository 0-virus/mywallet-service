package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.Policy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PolicyRepository extends JpaRepository<Policy, Long>, JpaSpecificationExecutor<Policy> {

    Optional<Policy> findBySourceTypeAndSourcePolicyId(String sourceType, String sourcePolicyId);
}
