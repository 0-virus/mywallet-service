package com.ragvirus.policy.application.normalization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class Gov24PolicyNormalizer {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ObjectMapper objectMapper;

    public Gov24PolicyNormalizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<NormalizedPolicy> normalizeDetail(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode first = root.path("data").isArray() && !root.path("data").isEmpty()
                    ? root.path("data").get(0)
                    : null;
            if (first == null || first.isMissingNode()) {
                return Optional.empty();
            }

            String serviceId = text(first, "서비스ID");
            String title = text(first, "서비스명");
            if (isBlank(serviceId) || isBlank(title)) {
                return Optional.empty();
            }

            LocalDate dueDate = parseLastDate(text(first, "신청기한"));
            boolean alwaysOpen = isAlwaysOpen(text(first, "신청기한"));

            return Optional.of(new NormalizedPolicy(
                    "gov24",
                    serviceId,
                    title,
                    firstNonBlank(text(first, "서비스목적"), text(first, "서비스목적요약")),
                    text(first, "소관기관명"),
                    firstNonBlank(text(first, "부서명"), text(first, "접수기관명")),
                    null,
                    text(first, "지원유형"),
                    text(first, "지원대상"),
                    text(first, "선정기준"),
                    text(first, "지원내용"),
                    text(first, "신청방법"),
                    firstNonBlank(text(first, "구비서류"), text(first, "본인확인필요구비서류")),
                    firstNonBlank(text(first, "문의처"), text(first, "전화문의")),
                    "national",
                    text(first, "상세조회URL"),
                    text(first, "온라인신청사이트URL"),
                    null,
                    dueDate,
                    alwaysOpen
            ));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to normalize Gov24 policy detail", ex);
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text.trim();
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isAlwaysOpen(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("상시") || value.contains("수시") || value.contains("연중");
    }

    private LocalDate parseLastDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replace(".", "-");
        String[] tokens = normalized.split("[^0-9-]+");
        LocalDate latest = null;
        for (String token : tokens) {
            if (!token.matches("\\d{4}-\\d{2}-\\d{2}")) {
                continue;
            }
            LocalDate parsed = LocalDate.parse(token, ISO_DATE);
            if (latest == null || parsed.isAfter(latest)) {
                latest = parsed;
            }
        }
        return latest;
    }
}
