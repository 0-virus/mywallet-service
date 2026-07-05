package com.ragvirus.policy.application.normalization;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DedupKeyGeneratorTest {

    private final DedupKeyGenerator generator = new DedupKeyGenerator();

    @Test
    void normalizesWhitespaceAndCase() {
        NormalizedPolicy first = policy("청년 내일 저축 계좌", "보건복지부");
        NormalizedPolicy second = policy("청년내일저축계좌", "보건 복지부");

        assertThat(generator.generate(first)).isEqualTo(generator.generate(second));
    }

    private NormalizedPolicy policy(String title, String agencyName) {
        return new NormalizedPolicy(
                "gov24",
                "P1",
                title,
                null,
                agencyName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "national",
                "https://example.com/detail",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 20),
                false
        );
    }
}
