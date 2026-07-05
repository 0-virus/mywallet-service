package com.ragvirus.policy.application;

import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.application.eligibility.EligibilityResult;
import com.ragvirus.policy.application.eligibility.PolicyEligibilityService;
import com.ragvirus.policy.application.eligibility.UserPolicyCondition;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyCondition;
import com.ragvirus.policy.repository.PolicyConditionRepository;
import com.ragvirus.policy.repository.PolicyRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyRecommendationService {

    private final PolicyRepository policyRepository;
    private final PolicyConditionRepository conditionRepository;
    private final PolicyEligibilityService eligibilityService;

    public PolicyRecommendationService(
            PolicyRepository policyRepository,
            PolicyConditionRepository conditionRepository,
            PolicyEligibilityService eligibilityService
    ) {
        this.policyRepository = policyRepository;
        this.conditionRepository = conditionRepository;
        this.eligibilityService = eligibilityService;
    }

    @Transactional(readOnly = true)
    public List<PolicyRecommendationResponse> recommend(UserPolicyCondition userCondition) {
        return policyRepository.findAll().stream()
                .filter(Policy::isActive)
                .filter(this::isYouthRecommendationCandidate)
                .map(policy -> toRecommendation(policy, userCondition))
                .filter(response -> !"ineligible".equals(response.eligibilityStatus()))
                .sorted(Comparator
                        .comparingInt(PolicyRecommendationResponse::score).reversed()
                        .thenComparing(response -> response.dday() == null ? Integer.MAX_VALUE : response.dday()))
                .limit(10)
                .toList();
    }

    private PolicyRecommendationResponse toRecommendation(Policy policy, UserPolicyCondition userCondition) {
        EligibilityResult eligibility = eligibilityService.evaluate(policy, userCondition);
        int score = score(policy, eligibility);
        return PolicyRecommendationResponse.of(policy, eligibility, score);
    }

    private int score(Policy policy, EligibilityResult eligibility) {
        int score = 0;
        if ("eligible".equals(eligibility.status())) {
            score += 40;
        }
        if ("need_check".equals(eligibility.status())) {
            score += 15;
        }
        if ("closing_soon".equals(policy.getApplicationStatus())) {
            score += 15;
        }
        if ("open".equals(policy.getApplicationStatus()) || "always_open".equals(policy.getApplicationStatus())) {
            score += 20;
        }
        if (hasYouthText(policy)) {
            score += 10;
        }
        if (hasAgeCondition(policy)) {
            score += 10;
        }
        score += eligibility.matchedReasons().size() * 5;
        score -= eligibility.needCheckReasons().size() * 2;
        return score;
    }

    private boolean isYouthRecommendationCandidate(Policy policy) {
        return hasYouthText(policy) || hasAgeCondition(policy);
    }

    private boolean hasAgeCondition(Policy policy) {
        return conditionRepository.findByPolicy_Id(policy.getId()).stream()
                .anyMatch(this::hasAgeCondition);
    }

    private boolean hasAgeCondition(PolicyCondition condition) {
        return condition.getMinAge() != null || condition.getMaxAge() != null;
    }

    private boolean hasYouthText(Policy policy) {
        String text = String.join(" ",
                nullToBlank(policy.getTitle()),
                nullToBlank(policy.getSummary()),
                nullToBlank(policy.getTargetText()),
                nullToBlank(policy.getCriteriaText()),
                nullToBlank(policy.getBenefitText())
        );
        return text.contains("청년") || text.contains("대학생") || text.contains("취준") || text.contains("구직");
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
