package com.ragvirus.policy.application.condition;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragvirus.policy.application.normalization.NormalizedPolicy;
import org.junit.jupiter.api.Test;

class PolicyConditionExtractorTest {

    private final PolicyConditionExtractor extractor = new PolicyConditionExtractor(new ObjectMapper());

    @Test
    void extractsGov24AgeCodesBeforeTextFallback() {
        NormalizedPolicy policy = policy("""
                신청 당시 만 19세 이상 34세 이하 청년
                기준 중위소득 50% 이하
                """);
        String supportConditions = """
                {
                  "data": [
                    {
                      "서비스ID": "135200005013",
                      "JA0110": 15,
                      "JA0111": 39,
                      "JA0201": "Y"
                    }
                  ]
                }
                """;

        ExtractedPolicyCondition condition = extractor.extractGov24(policy, supportConditions);

        assertThat(condition.minAge()).isEqualTo(15);
        assertThat(condition.maxAge()).isEqualTo(39);
        assertThat(condition.incomeBand()).isEqualTo("중위소득 50% 이하");
        assertThat(condition.needManualCheck()).isTrue();
    }

    @Test
    void extractsAgeRangeAndKeywordsFromDetailText() {
        NormalizedPolicy policy = policy("""
                서울 거주 만 19세 이상 34세 이하 구직 청년
                월세 임차 가구 대상
                """);

        ExtractedPolicyCondition condition = extractor.extractFromPolicyText(policy);

        assertThat(condition.minAge()).isEqualTo(19);
        assertThat(condition.maxAge()).isEqualTo(34);
        assertThat(condition.employmentStatus()).isEqualTo("구직");
        assertThat(condition.housingStatus()).isEqualTo("월세");
    }

    private NormalizedPolicy policy(String conditionText) {
        return new NormalizedPolicy(
                "gov24",
                "sample",
                "청년 지원 정책",
                "청년 지원 요약",
                "기관",
                "부서",
                null,
                null,
                conditionText,
                null,
                null,
                null,
                null,
                null,
                "national",
                null,
                null,
                null,
                null,
                false
        );
    }
}
