package com.ragvirus.policy.application.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocalHashingEmbeddingProviderTest {

    private final LocalHashingEmbeddingProvider provider = new LocalHashingEmbeddingProvider();

    @Test
    void embedsKoreanPolicyTextWithoutNegativeIndex() {
        double[] vector = provider.embed("청년내일저축계좌 기준 중위소득 50% 이하 근로 청년 지원");

        assertThat(vector).hasSize(128);
        assertThat(VectorMath.cosine(vector, vector)).isCloseTo(1.0d, org.assertj.core.data.Offset.offset(0.000001d));
    }
}
