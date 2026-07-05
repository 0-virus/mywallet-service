package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyDocumentChunk;
import com.ragvirus.policy.repository.PolicyDocumentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyChunkService {

    private final PolicyDocumentChunkRepository chunkRepository;

    public PolicyChunkService(PolicyDocumentChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public void rebuildChunks(Policy policy) {
        chunkRepository.deleteByPolicy_Id(policy.getId());
        chunkRepository.saveAll(createChunks(policy));
    }

    private List<PolicyDocumentChunk> createChunks(Policy policy) {
        List<PolicyDocumentChunk> chunks = new ArrayList<>();
        add(chunks, policy, "summary", policy.getSummary());
        add(chunks, policy, "target", policy.getTargetText());
        add(chunks, policy, "criteria", policy.getCriteriaText());
        add(chunks, policy, "benefit", policy.getBenefitText());
        add(chunks, policy, "apply", policy.getApplyText());
        add(chunks, policy, "contact", policy.getContactText());
        return chunks;
    }

    private void add(List<PolicyDocumentChunk> chunks, Policy policy, String type, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        String chunk = """
                [정책명] %s
                [유형] %s
                [내용]
                %s
                """.formatted(policy.getTitle(), type, content);
        chunks.add(new PolicyDocumentChunk(policy, type, chunk, "{\"sourceType\":\"%s\"}".formatted(policy.getSourceType())));
    }
}
