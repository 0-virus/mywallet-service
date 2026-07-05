package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "policy.rag.chat-provider", havingValue = "local", matchIfMissing = true)
public class LocalPolicyAnswerGenerator implements PolicyAnswerGenerator {

    @Override
    public String generate(
            String message,
            List<PolicyRecommendationResponse> recommendations,
            List<RetrievedPolicyChunk> retrievedChunks
    ) {
        if (recommendations.isEmpty() && retrievedChunks.isEmpty()) {
            return "현재 입력된 조건과 질문으로는 근거가 되는 정책을 찾지 못했습니다. 나이, 거주지, 고용 상태, 소득 구간이나 관심 분야를 조금 더 구체적으로 입력해 주세요.";
        }

        RetrievedPolicyChunk topEvidence = retrievedChunks.isEmpty() ? null : retrievedChunks.getFirst();
        if (recommendations.isEmpty() && topEvidence != null) {
            return "추천 조건으로 바로 판별된 정책은 없지만, 질문과 가까운 정책 원문은 '"
                    + topEvidence.policyTitle()
                    + "'의 "
                    + labelChunkType(topEvidence.chunkType())
                    + " 내용입니다. 신청 가능 여부를 판단하려면 나이, 거주지, 소득, 고용 상태를 추가로 입력해 주세요.";
        }

        PolicyRecommendationResponse top = recommendations.getFirst();
        StringBuilder answer = new StringBuilder();
        if (topEvidence != null && !topEvidence.policyId().equals(top.policyId())) {
            answer.append("질문 내용과 가장 가까운 정책 원문은 '")
                    .append(topEvidence.policyTitle())
                    .append("'의 ")
                    .append(labelChunkType(topEvidence.chunkType()))
                    .append("입니다. 다만 입력한 조건과 현재 신청 가능성까지 함께 보면 우선 확인할 추천 정책은 '")
                    .append(top.title())
                    .append("'입니다. ");
        } else {
            answer.append("입력한 조건과 질문을 기준으로 우선 확인할 정책은 '")
                    .append(top.title())
                    .append("'입니다. ");
        }
        if ("eligible".equals(top.eligibilityStatus())) {
            answer.append("현재 구조화된 조건 기준으로 신청 가능성이 높습니다. ");
        } else {
            answer.append("다만 일부 조건은 확인이 필요합니다. ");
        }
        if (!top.needCheckReasons().isEmpty()) {
            answer.append("확인 필요 항목: ")
                    .append(String.join(", ", top.needCheckReasons()))
                    .append(". ");
        }
        if (topEvidence != null && topEvidence.policyId().equals(top.policyId())) {
            answer.append("질문과 가장 가까운 근거는 '")
                    .append(topEvidence.policyTitle())
                    .append("'의 ")
                    .append(labelChunkType(topEvidence.chunkType()))
                    .append(" 내용입니다. ");
        }
        answer.append("답변은 저장된 정책 원문 chunk를 기준으로 했고, 최종 신청 가능 여부는 공식 링크와 신청방법을 함께 확인해야 합니다.");
        return answer.toString();
    }

    private String labelChunkType(String chunkType) {
        return switch (chunkType) {
            case "summary" -> "정책 요약";
            case "target" -> "지원대상";
            case "criteria" -> "선정기준";
            case "benefit" -> "지원내용";
            case "apply" -> "신청방법";
            case "contact" -> "문의처";
            default -> chunkType;
        };
    }
}
