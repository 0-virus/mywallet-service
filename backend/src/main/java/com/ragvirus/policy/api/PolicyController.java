package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.PolicyDetailResponse;
import com.ragvirus.policy.api.dto.PolicySummaryResponse;
import com.ragvirus.policy.application.PolicyQueryService;
import com.ragvirus.policy.application.PolicyQueryService.PolicySearchCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyQueryService policyQueryService;

    public PolicyController(PolicyQueryService policyQueryService) {
        this.policyQueryService = policyQueryService;
    }

    @GetMapping
    public Page<PolicySummaryResponse> search(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String applicationStatus,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return policyQueryService.search(
                new PolicySearchCommand(regionId, category, keyword, applicationStatus),
                pageable
        );
    }

    @GetMapping("/{policyId}")
    public PolicyDetailResponse detail(@PathVariable Long policyId) {
        return policyQueryService.getDetail(policyId);
    }
}
