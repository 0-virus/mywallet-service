package com.ragvirus.policy.application.condition;

import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyCondition;
import com.ragvirus.policy.repository.PolicyConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyConditionUpsertService {

    private final PolicyConditionRepository repository;

    public PolicyConditionUpsertService(PolicyConditionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void replaceCondition(Policy policy, ExtractedPolicyCondition extracted) {
        repository.deleteByPolicy_Id(policy.getId());
        PolicyCondition condition = new PolicyCondition(policy);
        condition.applyExtracted(
                extracted.minAge(),
                extracted.maxAge(),
                extracted.gender(),
                extracted.incomeBand(),
                extracted.employmentStatus(),
                extracted.studentStatus(),
                extracted.householdStatus(),
                extracted.housingStatus(),
                extracted.businessStatus(),
                extracted.conditionSummary(),
                extracted.needManualCheck(),
                extracted.missingFields(),
                extracted.conditionSource()
        );
        repository.save(condition);
    }
}
