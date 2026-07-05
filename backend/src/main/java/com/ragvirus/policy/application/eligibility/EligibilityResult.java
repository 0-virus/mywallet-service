package com.ragvirus.policy.application.eligibility;

import java.util.List;

public record EligibilityResult(
        String status,
        List<String> matchedReasons,
        List<String> needCheckReasons,
        List<String> rejectedReasons
) {
    public static EligibilityResult needCheck(String reason) {
        return new EligibilityResult("need_check", List.of(), List.of(reason), List.of());
    }
}
