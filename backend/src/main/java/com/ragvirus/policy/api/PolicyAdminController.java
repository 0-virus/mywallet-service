package com.ragvirus.policy.api;

import com.ragvirus.policy.application.sync.PolicySyncService;
import com.ragvirus.policy.domain.Policy;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/policies")
public class PolicyAdminController {

    private final PolicySyncService policySyncService;

    public PolicyAdminController(PolicySyncService policySyncService) {
        this.policySyncService = policySyncService;
    }

    @PostMapping("/gov24/{serviceId}/sync")
    public ResponseEntity<Map<String, Object>> syncGov24Policy(@PathVariable @NotBlank String serviceId) {
        return policySyncService.syncGov24Policy(serviceId)
                .map(this::synced)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "synced", false,
                        "sourceType", "gov24",
                        "sourcePolicyId", serviceId,
                        "reason", "No policy detail data returned"
                )));
    }

    @PostMapping("/local-welfare/{servId}/sync")
    public ResponseEntity<Map<String, Object>> syncLocalWelfarePolicy(@PathVariable @NotBlank String servId) {
        return policySyncService.syncLocalWelfarePolicy(servId)
                .map(this::synced)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "synced", false,
                        "sourceType", "local_welfare",
                        "sourcePolicyId", servId,
                        "reason", "No policy detail data returned"
                )));
    }

    private ResponseEntity<Map<String, Object>> synced(Policy policy) {
        return ResponseEntity.ok(Map.of(
                "synced", true,
                "policyId", policy.getId(),
                "sourceType", policy.getSourceType(),
                "sourcePolicyId", policy.getSourcePolicyId(),
                "title", policy.getTitle()
        ));
    }
}
