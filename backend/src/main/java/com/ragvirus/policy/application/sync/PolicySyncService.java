package com.ragvirus.policy.application.sync;

import com.ragvirus.policy.application.PolicySourceRawService;
import com.ragvirus.policy.application.PolicyUpsertService;
import com.ragvirus.policy.application.normalization.Gov24PolicyNormalizer;
import com.ragvirus.policy.application.rag.PolicyChunkService;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.infrastructure.client.Gov24PolicyClient;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PolicySyncService {

    private final Gov24PolicyClient gov24PolicyClient;
    private final Gov24PolicyNormalizer gov24PolicyNormalizer;
    private final PolicySourceRawService rawService;
    private final PolicyUpsertService upsertService;
    private final PolicyChunkService chunkService;

    public PolicySyncService(
            Gov24PolicyClient gov24PolicyClient,
            Gov24PolicyNormalizer gov24PolicyNormalizer,
            PolicySourceRawService rawService,
            PolicyUpsertService upsertService,
            PolicyChunkService chunkService
    ) {
        this.gov24PolicyClient = gov24PolicyClient;
        this.gov24PolicyNormalizer = gov24PolicyNormalizer;
        this.rawService = rawService;
        this.upsertService = upsertService;
        this.chunkService = chunkService;
    }

    public Optional<Policy> syncGov24Policy(String serviceId) {
        String detail = gov24PolicyClient.fetchServiceDetail(serviceId);
        rawService.saveJson("gov24", serviceId, "serviceDetail", detail);
        return gov24PolicyNormalizer.normalizeDetail(detail)
                .map(upsertService::upsert)
                .map(policy -> {
                    chunkService.rebuildChunks(policy);
                    return policy;
                });
    }
}
