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

    public UserPolicyProfile(Long memberId) {
        this.memberId = memberId;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
            LocalDate birthDate,
            Integer age,
            String incomeRange,
            String employmentStatus,
            Boolean studentStatus,
            String householdStatus,
            String housingStatus,
            String interestCategories,
            boolean notificationAgreed
    ) {
        this.birthDate = birthDate;
        this.age = age;
        this.incomeRange = incomeRange;
        this.employmentStatus = employmentStatus;
        this.studentStatus = studentStatus;
        this.householdStatus = householdStatus;
        this.housingStatus = housingStatus;
        this.interestCategories = interestCategories;
        this.notificationAgreed = notificationAgreed;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Integer getAge() {
        return age;
    }

    public String getIncomeRange() {
        return incomeRange;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public Boolean getStudentStatus() {
        return studentStatus;
    }

    public String getHouseholdStatus() {
        return householdStatus;
    }

    public String getHousingStatus() {
        return housingStatus;
    }

    public String getInterestCategories() {
        return interestCategories;
    }

    public boolean isNotificationAgreed() {
        return notificationAgreed;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
