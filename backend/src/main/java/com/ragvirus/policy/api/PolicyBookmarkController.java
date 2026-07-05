package com.ragvirus.policy.api;

import com.ragvirus.policy.api.dto.ChangeApplyStatusRequest;
import com.ragvirus.policy.api.dto.PolicyBookmarkResponse;
import com.ragvirus.policy.application.bookmark.PolicyBookmarkService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PolicyBookmarkController {

    private final PolicyBookmarkService bookmarkService;

    public PolicyBookmarkController(PolicyBookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/policies/{policyId}/bookmark")
    public PolicyBookmarkResponse addBookmark(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long policyId
    ) {
        return bookmarkService.addBookmark(memberId, policyId);
    }

    @GetMapping("/policy-bookmarks")
    public List<PolicyBookmarkResponse> bookmarks(@RequestHeader("X-Member-Id") Long memberId) {
        return bookmarkService.getBookmarks(memberId);
    }

    @PatchMapping("/policy-bookmarks/{bookmarkId}/apply-status")
    public PolicyBookmarkResponse changeApplyStatus(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long bookmarkId,
            @Valid @RequestBody ChangeApplyStatusRequest request
    ) {
        return bookmarkService.changeApplyStatus(memberId, bookmarkId, request.applyStatus());
    }

    @DeleteMapping("/policy-bookmarks/{bookmarkId}")
    public void deleteBookmark(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long bookmarkId
    ) {
        bookmarkService.deleteBookmark(memberId, bookmarkId);
    }
}
