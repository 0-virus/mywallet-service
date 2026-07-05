package com.ragvirus.policy.application;

import com.ragvirus.policy.application.normalization.ApplicationStatus;
import com.ragvirus.policy.application.normalization.ApplicationStatusCalculator;
import com.ragvirus.policy.application.normalization.DedupKeyGenerator;
import com.ragvirus.policy.application.normalization.NormalizedPolicy;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyUpsertService {

    private final PolicyRepository policyRepository;
    private final ApplicationStatusCalculator statusCalculator;
    private final DedupKeyGenerator dedupKeyGenerator;

    public PolicyUpsertService(
            PolicyRepository policyRepository,
            ApplicationStatusCalculator statusCalculator,
            DedupKeyGenerator dedupKeyGenerator
    ) {
        this.policyRepository = policyRepository;
        this.statusCalculator = statusCalculator;
        this.dedupKeyGenerator = dedupKeyGenerator;
    }

    @Transactional
    public Policy upsert(NormalizedPolicy normalized) {
        Policy policy = policyRepository
                .findBySourceTypeAndSourcePolicyId(normalized.sourceType(), normalized.sourcePolicyId())
                .orElseGet(() -> new Policy(normalized.sourceType(), normalized.sourcePolicyId(), normalized.title()));

        policy.applyNormalizedContent(
                normalized.title(),
                normalized.summary(),
                normalized.agencyName(),
                normalized.departmentName(),
                normalized.category(),
                normalized.supportType(),
                normalized.targetText(),
                normalized.criteriaText(),
                normalized.benefitText(),
                normalized.applyText(),
                normalized.requiredDocsText(),
                normalized.contactText()
        );
        policy.applyLinks(normalized.officialUrl(), normalized.applicationUrl());
        policy.applyRegion(normalized.regionScope());

        ApplicationStatus status = statusCalculator.calculate(
                normalized.startDate(),
                normalized.dueDate(),
                normalized.alwaysOpen()
        );
        policy.applySchedule(normalized.startDate(), normalized.dueDate(), status.status(), status.dday(), normalized.alwaysOpen());
        policy.markSynced(dedupKeyGenerator.generate(normalized));
        return policyRepository.save(policy);
    }
}
