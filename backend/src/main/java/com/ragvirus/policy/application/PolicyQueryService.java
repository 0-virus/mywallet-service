package com.ragvirus.policy.application;

import com.ragvirus.policy.api.dto.PolicyDetailResponse;
import com.ragvirus.policy.api.dto.PolicySummaryResponse;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.repository.PolicyRegionRepository;
import com.ragvirus.policy.repository.PolicyRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyQueryService {

    private final PolicyRepository policyRepository;
    private final PolicyRegionRepository policyRegionRepository;

    public PolicyQueryService(PolicyRepository policyRepository, PolicyRegionRepository policyRegionRepository) {
        this.policyRepository = policyRepository;
        this.policyRegionRepository = policyRegionRepository;
    }

    @Transactional(readOnly = true)
    public Page<PolicySummaryResponse> search(PolicySearchCommand command, Pageable pageable) {
        return policyRepository.findAll(specification(command), pageable)
                .map(PolicySummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PolicyDetailResponse getDetail(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        return PolicyDetailResponse.from(policy);
    }

    private Specification<Policy> specification(PolicySearchCommand command) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (hasText(command.keyword())) {
                String keyword = "%" + command.keyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("benefitText")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("agencyName")), keyword)
                ));
            }
            if (hasText(command.category())) {
                predicates.add(criteriaBuilder.equal(root.get("category"), command.category()));
            }
            if (hasText(command.applicationStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("applicationStatus"), command.applicationStatus()));
            }
            if (command.regionId() != null) {
                List<Long> policyIds = policyRegionRepository.findPolicyIdsByRegionId(command.regionId());
                predicates.add(criteriaBuilder.or(
                        root.get("regionScope").in("national", "unknown"),
                        policyIds.isEmpty()
                                ? criteriaBuilder.disjunction()
                                : root.get("id").in(policyIds)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record PolicySearchCommand(
            Long regionId,
            String category,
            String keyword,
            String applicationStatus
    ) {
    }
}
