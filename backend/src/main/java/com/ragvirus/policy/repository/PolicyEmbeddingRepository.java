package com.ragvirus.policy.repository;

import com.ragvirus.policy.domain.PolicyEmbedding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyEmbeddingRepository extends JpaRepository<PolicyEmbedding, Long> {

    void deleteByChunk_Policy_Id(Long policyId);

    List<PolicyEmbedding> findByEmbeddingModel(String embeddingModel);
}
