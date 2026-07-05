package com.ragvirus.policy.application.sync;

import com.ragvirus.policy.application.PolicySourceRawService;
import com.ragvirus.policy.application.PolicyUpsertService;
import com.ragvirus.policy.application.condition.ExtractedPolicyCondition;
import com.ragvirus.policy.application.condition.PolicyConditionExtractor;
import com.ragvirus.policy.application.condition.PolicyConditionUpsertService;
import com.ragvirus.policy.application.normalization.Gov24PolicyNormalizer;
import com.ragvirus.policy.application.normalization.LocalWelfarePolicyNormalizer;
import com.ragvirus.policy.application.normalization.NormalizedPolicy;
import com.ragvirus.policy.application.rag.PolicyChunkService;
import com.ragvirus.policy.application.rag.PolicyEmbeddingService;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.infrastructure.client.Gov24PolicyClient;
import com.ragvirus.policy.infrastructure.client.LocalWelfarePolicyClient;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PolicySyncService {

    private final Gov24PolicyClient gov24PolicyClient;
    private final LocalWelfarePolicyClient localWelfarePolicyClient;
    private final Gov24PolicyNormalizer gov24PolicyNormalizer;
    private final LocalWelfarePolicyNormalizer localWelfarePolicyNormalizer;
    private final PolicySourceRawService rawService;
    private final PolicyUpsertService upsertService;
    private final PolicyChunkService chunkService;
    private final PolicyEmbeddingService embeddingService;
    private final PolicyConditionExtractor conditionExtractor;
    private final PolicyConditionUpsertService conditionUpsertService;

    public PolicySyncService(
            Gov24PolicyClient gov24PolicyClient,
            LocalWelfarePolicyClient localWelfarePolicyClient,
            Gov24PolicyNormalizer gov24PolicyNormalizer,
            LocalWelfarePolicyNormalizer localWelfarePolicyNormalizer,
            PolicySourceRawService rawService,
            PolicyUpsertService upsertService,
            PolicyChunkService chunkService,
            PolicyEmbeddingService embeddingService,
            PolicyConditionExtractor conditionExtractor,
            PolicyConditionUpsertService conditionUpsertService
    ) {
        this.gov24PolicyClient = gov24PolicyClient;
        this.localWelfarePolicyClient = localWelfarePolicyClient;
        this.gov24PolicyNormalizer = gov24PolicyNormalizer;
        this.localWelfarePolicyNormalizer = localWelfarePolicyNormalizer;
        this.rawService = rawService;
        this.upsertService = upsertService;
        this.chunkService = chunkService;
        this.embeddingService = embeddingService;
        this.conditionExtractor = conditionExtractor;
        this.conditionUpsertService = conditionUpsertService;
    }

    public Optional<Policy> syncGov24Policy(String serviceId) {
        String detail = gov24PolicyClient.fetchServiceDetail(serviceId);
        String supportConditions = gov24PolicyClient.fetchSupportConditions(serviceId);
        rawService.saveJson("gov24", serviceId, "serviceDetail", detail);
        rawService.saveJson("gov24", serviceId, "supportConditions", supportConditions);
        return gov24PolicyNormalizer.normalizeDetail(detail)
                .map(normalized -> {
                    Policy policy = upsertService.upsert(normalized);
                    ExtractedPolicyCondition condition = conditionExtractor.extractGov24(normalized, supportConditions);
                    conditionUpsertService.replaceCondition(policy, condition);
                    return policy;
                })
                .map(policy -> {
                    chunkService.rebuildChunks(policy);
                    embeddingService.rebuildEmbeddings(policy);
                    return policy;
                });
    }

    public Optional<Policy> syncLocalWelfarePolicy(String servId) {
        String detail = localWelfarePolicyClient.fetchWelfareDetail(servId);
        rawService.saveXml("local_welfare", servId, "LcgvWelfaredetailed", detail);
        return localWelfarePolicyNormalizer.normalizeDetail(detail)
                .map(this::upsertLocalWelfare)
                .map(policy -> {
                    chunkService.rebuildChunks(policy);
                    embeddingService.rebuildEmbeddings(policy);
                    return policy;
                });
    }

    private Policy upsertLocalWelfare(NormalizedPolicy normalized) {
        Policy policy = upsertService.upsert(normalized);
        ExtractedPolicyCondition condition = conditionExtractor.extractFromPolicyText(normalized);
        conditionUpsertService.replaceCondition(policy, condition);
        return policy;
    }
}
