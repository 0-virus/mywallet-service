package com.ragvirus.policy.application.normalization;

import java.text.Normalizer;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DedupKeyGenerator {

    public String generate(NormalizedPolicy policy) {
        return normalize(policy.title())
                + "|" + normalize(policy.agencyName())
                + "|" + normalize(policy.regionScope())
                + "|" + normalize(date(policy.startDate()))
                + "|" + normalize(date(policy.dueDate()))
                + "|" + normalize(firstNonBlank(policy.applicationUrl(), policy.officialUrl()));
    }

    private String date(LocalDate date) {
        return date == null ? "" : date.toString();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC);
        return normalized.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}:/._-]", "");
    }
}
