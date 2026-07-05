package com.ragvirus.policy.application.condition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragvirus.policy.application.normalization.NormalizedPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PolicyConditionExtractor {

    private static final Pattern AGE_RANGE = Pattern.compile("(?:만\\s*)?(\\d{1,2})\\s*세\\s*(?:이상|부터|~|-|∼|에서)\\s*(?:만\\s*)?(\\d{1,2})\\s*세\\s*(?:이하|미만|까지)?");
    private static final Pattern MIN_AGE = Pattern.compile("(?:만\\s*)?(\\d{1,2})\\s*세\\s*(?:이상|부터)");
    private static final Pattern MAX_AGE = Pattern.compile("(?:만\\s*)?(\\d{1,2})\\s*세\\s*(?:이하|미만|까지)");
    private static final Pattern MEDIAN_INCOME = Pattern.compile("중위소득\\s*(\\d{1,3})\\s*%\\s*이하");

    private final ObjectMapper objectMapper;

    public PolicyConditionExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ExtractedPolicyCondition extractGov24(NormalizedPolicy policy, String supportConditionsJson) {
        Integer minAge = null;
        Integer maxAge = null;
        boolean hasSupportConditionCodes = false;

        try {
            JsonNode first = firstDataNode(supportConditionsJson);
            if (first != null) {
                minAge = intOrNull(first, "JA0110");
                maxAge = intOrNull(first, "JA0111");
                hasSupportConditionCodes = hasAnyYCode(first);
            }
        } catch (Exception ignored) {
            hasSupportConditionCodes = true;
        }

        TextExtraction text = extractFromText(joinPolicyConditionText(policy));
        minAge = firstNonNull(minAge, text.minAge());
        maxAge = firstNonNull(maxAge, text.maxAge());

        List<String> missing = new ArrayList<>();
        if (text.incomeBand() != null) {
            missing.add("소득 증빙 확인");
        }
        if (hasSupportConditionCodes) {
            missing.add("Gov24 지원조건 코드 세부 해석");
        }
        if (minAge == null && maxAge == null) {
            missing.add("연령 조건");
        }

        return new ExtractedPolicyCondition(
                minAge,
                maxAge,
                null,
                text.incomeBand(),
                text.employmentStatus(),
                text.studentStatus(),
                text.householdStatus(),
                text.housingStatus(),
                text.businessStatus(),
                summarize(policy),
                !missing.isEmpty(),
                toJsonArray(missing),
                "gov24_support_conditions"
        );
    }

    public ExtractedPolicyCondition extractFromPolicyText(NormalizedPolicy policy) {
        TextExtraction text = extractFromText(joinPolicyConditionText(policy));
        List<String> missing = new ArrayList<>();
        if (text.minAge() == null && text.maxAge() == null) {
            missing.add("연령 조건");
        }
        if (text.incomeBand() != null) {
            missing.add("소득 증빙 확인");
        }
        missing.add("상세 조건 원문 확인");

        return new ExtractedPolicyCondition(
                text.minAge(),
                text.maxAge(),
                null,
                text.incomeBand(),
                text.employmentStatus(),
                text.studentStatus(),
                text.householdStatus(),
                text.housingStatus(),
                text.businessStatus(),
                summarize(policy),
                true,
                toJsonArray(missing),
                "detail_text"
        );
    }

    private TextExtraction extractFromText(String text) {
        Integer minAge = null;
        Integer maxAge = null;

        Matcher ageRange = AGE_RANGE.matcher(text);
        if (ageRange.find()) {
            minAge = parseInt(ageRange.group(1));
            maxAge = parseInt(ageRange.group(2));
        } else {
            Matcher minAgeMatcher = MIN_AGE.matcher(text);
            if (minAgeMatcher.find()) {
                minAge = parseInt(minAgeMatcher.group(1));
            }
            Matcher maxAgeMatcher = MAX_AGE.matcher(text);
            if (maxAgeMatcher.find()) {
                maxAge = parseInt(maxAgeMatcher.group(1));
            }
        }

        Matcher incomeMatcher = MEDIAN_INCOME.matcher(text);
        String incomeBand = incomeMatcher.find() ? "중위소득 " + incomeMatcher.group(1) + "% 이하" : null;

        return new TextExtraction(
                minAge,
                maxAge,
                incomeBand,
                firstKeyword(text, "구직", "근로", "재직", "취업", "창업", "어업경영"),
                containsAny(text, "대학생", "재학생", "휴학생") ? Boolean.TRUE : null,
                firstKeyword(text, "1인가구", "한부모", "다자녀", "신혼부부"),
                firstKeyword(text, "무주택", "전세", "월세", "임차"),
                firstKeyword(text, "창업", "사업자", "소상공인", "어업경영")
        );
    }

    private JsonNode firstDataNode(String rawJson) throws Exception {
        JsonNode data = objectMapper.readTree(rawJson).path("data");
        if (!data.isArray() || data.isEmpty()) {
            return null;
        }
        return data.get(0);
    }

    private boolean hasAnyYCode(JsonNode node) {
        for (JsonNode value : node) {
            if ("Y".equalsIgnoreCase(value.asText())) {
                return true;
            }
        }
        return false;
    }

    private Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.canConvertToInt() ? value.asInt() : parseInt(value.asText());
    }

    private String joinPolicyConditionText(NormalizedPolicy policy) {
        return String.join("\n",
                nullToBlank(policy.targetText()),
                nullToBlank(policy.criteriaText()),
                nullToBlank(policy.summary()),
                nullToBlank(policy.benefitText())
        );
    }

    private String summarize(NormalizedPolicy policy) {
        String summary = firstNonBlank(policy.targetText(), policy.criteriaText());
        return firstNonBlank(summary, policy.summary());
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String firstKeyword(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    private String toJsonArray(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }

    private String firstNonBlank(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private record TextExtraction(
            Integer minAge,
            Integer maxAge,
            String incomeBand,
            String employmentStatus,
            Boolean studentStatus,
            String householdStatus,
            String housingStatus,
            String businessStatus
    ) {
    }
}
