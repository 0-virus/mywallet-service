package com.ragvirus.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "policy_source_raw")
public class PolicySourceRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String sourceType;

    @Column(nullable = false, length = 100)
    private String sourceServiceId;

    @Column(nullable = false, length = 100)
    private String endpoint;

    private Instant fetchedAt = Instant.now();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String rawData;

    protected PolicySourceRaw() {
    }

    public PolicySourceRaw(String sourceType, String sourceServiceId, String endpoint, String rawData) {
        this.sourceType = sourceType;
        this.sourceServiceId = sourceServiceId;
        this.endpoint = endpoint;
        this.rawData = rawData;
    }
}
