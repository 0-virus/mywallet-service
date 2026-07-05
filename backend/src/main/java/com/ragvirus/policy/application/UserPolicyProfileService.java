package com.ragvirus.policy.application;

import com.ragvirus.policy.api.dto.UserPolicyProfileRequest;
import com.ragvirus.policy.domain.UserPolicyProfile;
import com.ragvirus.policy.repository.UserPolicyProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserPolicyProfileService {

    private final UserPolicyProfileRepository repository;

    public UserPolicyProfileService(UserPolicyProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserPolicyProfile get(Long memberId) {
        return repository.findByMemberId(memberId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Policy profile not found"));
    }

    @Transactional
    public UserPolicyProfile upsert(Long memberId, UserPolicyProfileRequest request) {
        UserPolicyProfile profile = repository.findByMemberId(memberId)
                .orElseGet(() -> new UserPolicyProfile(memberId));
        profile.update(
                request.birthDate(),
                request.age(),
                request.incomeRange(),
                request.employmentStatus(),
                request.studentStatus(),
                request.householdStatus(),
                request.housingStatus(),
                request.interestCategories(),
                request.notificationAgreed()
        );
        return repository.save(profile);
    }
}
