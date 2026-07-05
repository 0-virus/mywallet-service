package com.ragvirus.policy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "policy.public-data")
public record PolicyDataProperties(
        String serviceKey,
        String gov24BaseUrl,
        String localWelfareBaseUrl
) {
    public boolean hasServiceKey() {
        return serviceKey != null && !serviceKey.isBlank();
    }
}
