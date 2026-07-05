package com.ragvirus.policy.domain;

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
import java.time.LocalDate;

@Entity
@Table(name = "user_policy_profile")
public class UserPolicyProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residence_region_id")
    private RegionCode residenceRegion;

    private LocalDate birthDate;
    private Integer age;
    private String incomeRange;
    private String employmentStatus;
    private Boolean studentStatus;
    private String householdStatus;
    private String housingStatus;
    private String interestCategories;
    private boolean notificationAgreed;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected UserPolicyProfile() {
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
