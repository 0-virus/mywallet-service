package com.ragvirus.policy.api.dto;

import com.ragvirus.policy.application.rag.RetrievedPolicyChunk;

public record PolicyChatCitationResponse(
        Long policyId,
        String policyTitle,
        String chunkType,
        String content,
        String officialUrl,
        double similarityScore
) {

    public static PolicyChatCitationResponse from(RetrievedPolicyChunk chunk) {
        return new PolicyChatCitationResponse(
                chunk.policyId(),
                chunk.policyTitle(),
                chunk.chunkType(),
                chunk.content(),
                chunk.officialUrl(),
                chunk.similarityScore()
        );
    }
}
