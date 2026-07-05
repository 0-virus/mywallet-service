package com.ragvirus.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String sourceType;

    @Column(nullable = false, length = 100)
    private String sourcePolicyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 150)
    private String agencyName;

    @Column(length = 150)
    private String departmentName;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String supportType;

    @Column(columnDefinition = "TEXT")
    private String targetText;

    @Column(columnDefinition = "TEXT")
    private String criteriaText;

    @Column(columnDefinition = "TEXT")
    private String benefitText;

    @Column(columnDefinition = "TEXT")
    private String applyText;

    @Column(columnDefinition = "TEXT")
    private String requiredDocsText;

    @Column(columnDefinition = "TEXT")
    private String contactText;

    @Column(nullable = false, length = 30)
    private String regionScope = "unknown";

    @Column(length = 700)
    private String officialUrl;

    @Column(length = 700)
    private String applicationUrl;

    private LocalDate startDate;
    private LocalDate dueDate;

    @Column(nullable = false, length = 30)
    private String applicationStatus = "unknown";

    private Integer dday;

    @Column(name = "is_always_open", nullable = false)
    private boolean alwaysOpen;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(length = 700)
    private String dedupKey;

    private Instant lastSyncedAt;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    protected Policy() {
    }

    public Policy(String sourceType, String sourcePolicyId, String title) {
        this.sourceType = sourceType;
        this.sourcePolicyId = sourcePolicyId;
        this.title = title;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourcePolicyId() {
        return sourcePolicyId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getCategory() {
        return category;
    }

    public String getSupportType() {
        return supportType;
    }

    public String getTargetText() {
        return targetText;
    }

    public String getCriteriaText() {
        return criteriaText;
    }

    public String getBenefitText() {
        return benefitText;
    }

    public String getApplyText() {
        return applyText;
    }

    public String getRequiredDocsText() {
        return requiredDocsText;
    }

    public String getContactText() {
        return contactText;
    }

    public String getRegionScope() {
        return regionScope;
    }

    public String getOfficialUrl() {
        return officialUrl;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public Integer getDday() {
        return dday;
    }

    public boolean isAlwaysOpen() {
        return alwaysOpen;
    }

    public boolean isActive() {
        return active;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void applyNormalizedContent(
            String title,
            String summary,
            String agencyName,
            String departmentName,
            String category,
            String supportType,
            String targetText,
            String criteriaText,
            String benefitText,
            String applyText,
            String requiredDocsText,
            String contactText
    ) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        this.summary = summary;
        this.agencyName = agencyName;
        this.departmentName = departmentName;
        this.category = category;
        this.supportType = supportType;
        this.targetText = targetText;
        this.criteriaText = criteriaText;
        this.benefitText = benefitText;
        this.applyText = applyText;
        this.requiredDocsText = requiredDocsText;
        this.contactText = contactText;
    }

    public void applyLinks(String officialUrl, String applicationUrl) {
        this.officialUrl = officialUrl;
        this.applicationUrl = applicationUrl;
    }

    public void applySchedule(LocalDate startDate, LocalDate dueDate, String applicationStatus, Integer dday, boolean alwaysOpen) {
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.applicationStatus = applicationStatus;
        this.dday = dday;
        this.alwaysOpen = alwaysOpen;
    }

    public void applyRegion(String regionScope) {
        this.regionScope = regionScope == null ? "unknown" : regionScope;
    }

    public void markSynced(String dedupKey) {
        this.dedupKey = dedupKey;
        this.lastSyncedAt = Instant.now();
    }
}
