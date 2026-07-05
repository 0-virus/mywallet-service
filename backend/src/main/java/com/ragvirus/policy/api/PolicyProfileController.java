package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.UserPolicyProfileRequest;
import com.ragvirus.policy.api.dto.UserPolicyProfileResponse;
import com.ragvirus.policy.application.UserPolicyProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/policy/profile")
public class PolicyProfileController {

    private final UserPolicyProfileService profileService;

    public PolicyProfileController(UserPolicyProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserPolicyProfileResponse getProfile(@RequestHeader("X-Member-Id") Long memberId) {
        return UserPolicyProfileResponse.from(profileService.get(memberId));
    }

    @PostMapping
    public UserPolicyProfileResponse createOrReplaceProfile(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody UserPolicyProfileRequest request
    ) {
        return UserPolicyProfileResponse.from(profileService.upsert(memberId, request));
    }

    @PatchMapping
    public UserPolicyProfileResponse updateProfile(
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody UserPolicyProfileRequest request
    ) {
        return UserPolicyProfileResponse.from(profileService.upsert(memberId, request));
    }
}
