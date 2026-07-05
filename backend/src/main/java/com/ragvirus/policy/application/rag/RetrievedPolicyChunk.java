package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyDocumentChunk;

public record RetrievedPolicyChunk(
        Long policyId,
        String policyTitle,
        String chunkType,
        String content,
        String officialUrl,
        double similarityScore
) {

    public static RetrievedPolicyChunk of(PolicyDocumentChunk chunk, double similarityScore) {
        Policy policy = chunk.getPolicy();
        return new RetrievedPolicyChunk(
                policy.getId(),
                policy.getTitle(),
                chunk.getChunkType(),
                chunk.getContent(),
                policy.getOfficialUrl(),
                similarityScore
        );
    }
}
