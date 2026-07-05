package com.ragvirus.policy.application.normalization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LocalWelfarePolicyNormalizer {

    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final XmlMapper xmlMapper;

    public LocalWelfarePolicyNormalizer() {
        this.xmlMapper = new XmlMapper();
    }

    public Optional<NormalizedPolicy> normalizeDetail(String rawXml) {
        try {
            JsonNode root = xmlMapper.readTree(rawXml);
            String serviceId = text(root, "servId");
            String title = text(root, "servNm");
            if (isBlank(serviceId) || isBlank(title)) {
                return Optional.empty();
            }

            LocalDate startDate = parseBasicDate(text(root, "enfcBgngYmd"));
            LocalDate dueDate = parseBasicDate(text(root, "enfcEndYmd"));
            boolean alwaysOpen = dueDate != null && dueDate.getYear() >= 9999;
            if (alwaysOpen) {
                dueDate = null;
            }

            return Optional.of(new NormalizedPolicy(
                    "local_welfare",
                    serviceId,
                    title,
                    text(root, "servDgst"),
                    firstNonBlank(text(root, "ctpvNm"), "지자체"),
                    text(root, "bizChrDeptNm"),
                    text(root, "intrsThemaNmArray"),
                    text(root, "srvPvsnNm"),
                    text(root, "sprtTrgtCn"),
                    text(root, "slctCritCn"),
                    text(root, "alwServCn"),
                    firstNonBlank(text(root, "aplyMtdCn"), text(root, "aplyMtdNm")),
                    extractRelatedNames(root, "basfrmList"),
                    extractRelatedNames(root, "inqplCtadrList"),
                    regionScope(text(root, "ctpvNm"), text(root, "sggNm")),
                    text(root, "servDtlLink"),
                    null,
                    startDate,
                    dueDate,
                    alwaysOpen
            ));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to normalize local welfare policy detail", ex);
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isValueNode()) {
            String text = value.asText();
            return isBlank(text) ? null : text.trim();
        }
        return value.toString();
    }

    private String extractRelatedNames(JsonNode root, String fieldName) {
        JsonNode value = root.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : value) {
                appendRelated(builder, item);
            }
            return builder.isEmpty() ? null : builder.toString();
        }
        StringBuilder builder = new StringBuilder();
        appendRelated(builder, value);
        return builder.isEmpty() ? null : builder.toString();
    }

    private void appendRelated(StringBuilder builder, JsonNode item) {
        String name = text(item, "wlfareInfoReldNm");
        String content = text(item, "wlfareInfoReldCn");
        if (isBlank(name) && isBlank(content)) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append("||");
        }
        builder.append(firstNonBlank(name, ""));
        if (!isBlank(content)) {
            builder.append("/").append(content);
        }
    }

    private String firstNonBlank(String first, String second) {
        return isBlank(first) ? second : first;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private LocalDate parseBasicDate(String value) {
        if (isBlank(value) || !value.matches("\\d{8}")) {
            return null;
        }
        return LocalDate.parse(value, BASIC_DATE);
    }

    private String regionScope(String ctpvNm, String sggNm) {
        if (!isBlank(ctpvNm) && !isBlank(sggNm)) {
            return "sigungu";
        }
        if (!isBlank(ctpvNm)) {
            return "sido";
        }
        return "unknown";
    }
}
