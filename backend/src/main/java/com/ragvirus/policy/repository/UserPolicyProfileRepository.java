package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.UserPolicyProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPolicyProfileRepository extends JpaRepository<UserPolicyProfile, Long> {

    Optional<UserPolicyProfile> findByMemberId(Long memberId);
}
