package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.PolicyRecommendationRequest;
import com.ragvirus.policy.api.dto.PolicyRecommendationResponse;
import com.ragvirus.policy.application.PolicyRecommendationService;
import com.ragvirus.policy.application.eligibility.UserPolicyCondition;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies/recommendations")
public class PolicyRecommendationController {

    private final PolicyRecommendationService recommendationService;

    public PolicyRecommendationController(PolicyRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<PolicyRecommendationResponse> recommend(@RequestBody PolicyRecommendationRequest request) {
        return recommendationService.recommend(new UserPolicyCondition(
                request.age(),
                request.regionId(),
                request.incomeRange(),
                request.employmentStatus(),
                request.studentStatus(),
                request.householdStatus(),
                request.housingStatus(),
                request.interestCategories()
        ));
    }
}
