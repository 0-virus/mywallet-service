package com.ragvirus.policy.application.rag;

import com.ragvirus.policy.api.dto.PolicyChatRequest;
import com.ragvirus.policy.api.dto.PolicyChatResponse;
import com.ragvirus.policy.api.dto.PolicyRecommendationRequest;
import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.application.PolicyRecommendationService;
import com.ragvirus.policy.application.eligibility.UserPolicyCondition;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PolicyChatService {

    private final PolicyRecommendationService recommendationService;

    public PolicyChatService(PolicyRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public PolicyChatResponse answer(PolicyChatRequest request) {
        PolicyRecommendationRequest profile = request.profile() == null
                ? new PolicyRecommendationRequest(null, null, null, null, null, null, null, null)
                : request.profile();

        List<PolicyRecommendationResponse> recommendations = recommendationService.recommend(new UserPolicyCondition(
                profile.age(),
                profile.regionId(),
                profile.incomeRange(),
                profile.employmentStatus(),
                profile.studentStatus(),
                profile.householdStatus(),
                profile.housingStatus(),
                profile.interestCategories()
        ));

        return new PolicyChatResponse(buildAnswer(request.message(), recommendations), recommendations);
    }

    private String buildAnswer(String message, List<PolicyRecommendationResponse> recommendations) {
        if (recommendations.isEmpty()) {
            return "현재 입력된 조건으로는 바로 추천할 정책을 찾지 못했습니다. 나이, 거주지, 고용 상태, 소득 구간을 추가로 입력하면 더 정확히 확인할 수 있습니다.";
        }
        PolicyRecommendationResponse top = recommendations.getFirst();
        StringBuilder answer = new StringBuilder();
        answer.append("입력한 조건을 기준으로 우선 확인할 정책은 '")
                .append(top.title())
                .append("'입니다. ");
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
        answer.append("정책 상세 화면에서 지원대상, 선정기준, 신청방법과 공식 링크를 함께 확인하세요.");
        return answer.toString();
    }
}
