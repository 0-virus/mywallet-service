package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PolicyRegionRepository extends JpaRepository<PolicyRegion, Long> {

    @Query("select pr.policy.id from PolicyRegion pr where pr.region.id = :regionId")
    List<Long> findPolicyIdsByRegionId(Long regionId);
}
