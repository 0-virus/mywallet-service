package com.ragvirus.policy.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeApplyStatusRequest(@NotBlank String applyStatus) {
}
