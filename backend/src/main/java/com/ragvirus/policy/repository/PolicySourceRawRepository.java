package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicySourceRaw;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicySourceRawRepository extends JpaRepository<PolicySourceRaw, Long> {

    Optional<PolicySourceRaw> findBySourceTypeAndSourceServiceIdAndEndpoint(
            String sourceType,
            String sourceServiceId,
            String endpoint
    );
}
