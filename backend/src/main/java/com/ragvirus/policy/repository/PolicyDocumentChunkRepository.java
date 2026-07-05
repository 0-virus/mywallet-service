package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyDocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentChunkRepository extends JpaRepository<PolicyDocumentChunk, Long> {

    void deleteByPolicy_Id(Long policyId);

    List<PolicyDocumentChunk> findByPolicy_Id(Long policyId);
}
