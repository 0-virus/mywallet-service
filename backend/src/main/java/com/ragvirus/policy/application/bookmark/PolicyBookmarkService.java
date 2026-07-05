package com.ragvirus.policy.application.bookmark;

import com.ragvirus.policy.api.dto.PolicyBookmarkResponse;
import com.ragvirus.policy.domain.Policy;
import com.ragvirus.policy.domain.PolicyBookmark;
import com.ragvirus.policy.domain.PolicyCalendarEvent;
import com.ragvirus.policy.repository.PolicyBookmarkRepository;
import com.ragvirus.policy.repository.PolicyCalendarEventRepository;
import com.ragvirus.policy.repository.PolicyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyBookmarkService {

    private final PolicyRepository policyRepository;
    private final PolicyBookmarkRepository bookmarkRepository;
    private final PolicyCalendarEventRepository calendarEventRepository;

    public PolicyBookmarkService(
            PolicyRepository policyRepository,
            PolicyBookmarkRepository bookmarkRepository,
            PolicyCalendarEventRepository calendarEventRepository
    ) {
        this.policyRepository = policyRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.calendarEventRepository = calendarEventRepository;
    }

    @Transactional
    public PolicyBookmarkResponse addBookmark(Long memberId, Long policyId) {
        if (bookmarkRepository.existsByMemberIdAndPolicy_Id(memberId, policyId)) {
            return bookmarkRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                    .filter(bookmark -> bookmark.getPolicy().getId().equals(policyId))
                    .findFirst()
                    .map(PolicyBookmarkResponse::from)
                    .orElseThrow();
        }

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        PolicyBookmark bookmark = bookmarkRepository.save(new PolicyBookmark(memberId, policy));
        createCalendarEvents(memberId, policy);
        return PolicyBookmarkResponse.from(bookmark);
    }

    @Transactional(readOnly = true)
    public List<PolicyBookmarkResponse> getBookmarks(Long memberId) {
        return bookmarkRepository.findByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(PolicyBookmarkResponse::from)
                .toList();
    }

    @Transactional
    public PolicyBookmarkResponse changeApplyStatus(Long memberId, Long bookmarkId, String applyStatus) {
        PolicyBookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found: " + bookmarkId));
        if (!bookmark.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("Cannot modify another member's policy bookmark");
        }
        bookmark.changeApplyStatus(applyStatus);
        return PolicyBookmarkResponse.from(bookmark);
    }

    private void createCalendarEvents(Long memberId, Policy policy) {
        if (policy.getStartDate() != null) {
            calendarEventRepository.save(new PolicyCalendarEvent(
                    memberId,
                    policy,
                    "application_start",
                    policy.getStartDate(),
                    policy.getTitle() + " 신청 시작"
            ));
        }
        if (policy.getDueDate() != null) {
            calendarEventRepository.save(new PolicyCalendarEvent(
                    memberId,
                    policy,
                    "application_due",
                    policy.getDueDate(),
                    policy.getTitle() + " 신청 마감"
            ));
        }
    }
}
