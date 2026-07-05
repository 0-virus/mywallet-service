package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyBookmark;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyBookmarkRepository extends JpaRepository<PolicyBookmark, Long> {

    boolean existsByMemberIdAndPolicy_Id(Long memberId, Long policyId);

    List<PolicyBookmark> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
