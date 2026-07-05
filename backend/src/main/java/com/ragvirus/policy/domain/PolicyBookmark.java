package com.ragvirus.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "policy_bookmark")
public class PolicyBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(nullable = false, length = 30)
    private String applyStatus = "planned";

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    @Column(length = 500)
    private String note;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected PolicyBookmark() {
    }

    public PolicyBookmark(Long memberId, Policy policy) {
        this.memberId = memberId;
        this.policy = policy;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Policy getPolicy() {
        return policy;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void changeApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }
}
