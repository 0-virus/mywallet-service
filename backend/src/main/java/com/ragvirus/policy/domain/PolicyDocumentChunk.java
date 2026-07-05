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
@Table(name = "policy_document_chunk")
public class PolicyDocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy;

    @Column(nullable = false, length = 50)
    private String chunkType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    private Instant createdAt = Instant.now();

    protected PolicyDocumentChunk() {
    }

    public PolicyDocumentChunk(Policy policy, String chunkType, String content, String metadata) {
        this.policy = policy;
        this.chunkType = chunkType;
        this.content = content;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public String getChunkType() {
        return chunkType;
    }

    public String getContent() {
        return content;
    }

    public String getMetadata() {
        return metadata;
    }
}
