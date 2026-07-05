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
@Table(name = "policy_embedding")
public class PolicyEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chunk_id")
    private PolicyDocumentChunk chunk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String embeddingJson;

    @Column(nullable = false, length = 100)
    private String embeddingModel;

    private Instant createdAt = Instant.now();

    protected PolicyEmbedding() {
    }

    public PolicyEmbedding(PolicyDocumentChunk chunk, String embeddingJson, String embeddingModel) {
        this.chunk = chunk;
        this.embeddingJson = embeddingJson;
        this.embeddingModel = embeddingModel;
    }
}
