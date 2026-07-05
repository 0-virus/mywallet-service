package com.ragvirus.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "policy_condition")
public class PolicyCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    private Integer minAge;
    private Integer maxAge;
    private String gender;
    private String incomeBand;
    private String employmentStatus;
    private Boolean studentStatus;
    private String householdStatus;
    private String housingStatus;
    private String businessStatus;

    @Column(columnDefinition = "TEXT")
    private String conditionSummary;

    @Column(nullable = false)
    private boolean needManualCheck;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String missingFields;

    private String conditionSource;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected PolicyCondition() {
    }

    public PolicyCondition(Policy policy) {
        this.policy = policy;
    }

    public void applyExtracted(
            Integer minAge,
            Integer maxAge,
            String gender,
            String incomeBand,
            String employmentStatus,
            Boolean studentStatus,
            String householdStatus,
            String housingStatus,
            String businessStatus,
            String conditionSummary,
            boolean needManualCheck,
            String missingFields,
            String conditionSource
    ) {
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.gender = gender;
        this.incomeBand = incomeBand;
        this.employmentStatus = employmentStatus;
        this.studentStatus = studentStatus;
        this.householdStatus = householdStatus;
        this.housingStatus = housingStatus;
        this.businessStatus = businessStatus;
        this.conditionSummary = conditionSummary;
        this.needManualCheck = needManualCheck;
        this.missingFields = missingFields;
        this.conditionSource = conditionSource;
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public String getGender() {
        return gender;
    }

    public String getIncomeBand() {
        return incomeBand;
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

    public String getBusinessStatus() {
        return businessStatus;
    }

    public boolean isNeedManualCheck() {
        return needManualCheck;
    }

    public String getConditionSummary() {
        return conditionSummary;
    }
}
