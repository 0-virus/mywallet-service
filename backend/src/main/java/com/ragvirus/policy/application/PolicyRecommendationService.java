package com.ragvirus.policy.application;

import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.application.eligibility.EligibilityResult;
import com.ragvirus.policy.application.eligibility.PolicyEligibilityService;
import com.ragvirus.policy.application.eligibility.UserPolicyCondition;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.repository.PolicyRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyRecommendationService {

    private final PolicyRepository policyRepository;
    private final PolicyEligibilityService eligibilityService;

    public PolicyRecommendationService(PolicyRepository policyRepository, PolicyEligibilityService eligibilityService) {
        this.policyRepository = policyRepository;
        this.eligibilityService = eligibilityService;
    }

    @Transactional(readOnly = true)
    public List<PolicyRecommendationResponse> recommend(UserPolicyCondition userCondition) {
        return policyRepository.findAll().stream()
                .filter(Policy::isActive)
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
        score += eligibility.matchedReasons().size() * 5;
        score -= eligibility.needCheckReasons().size() * 2;
        return score;
    }
}
