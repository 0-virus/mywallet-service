package com.ragvirus.policy.infrastructure.client;

import com.ragvirus.policy.config.PolicyDataProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
public class Gov24PolicyClient {

    private final PolicyDataProperties properties;
    private final RestClient restClient;

    public Gov24PolicyClient(PolicyDataProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.gov24BaseUrl()).build();
    }

    public String fetchServiceList(int page, int perPage, String keyword) {
        return get("/gov24/v3/serviceList", builder -> {
            builder.queryParam("page", page)
                    .queryParam("perPage", perPage)
                    .queryParam("serviceKey", properties.serviceKey());
            if (keyword != null && !keyword.isBlank()) {
                builder.queryParam("cond[서비스명::LIKE]", keyword);
            }
            return builder;
        });
    }

    public String fetchServiceDetail(String serviceId) {
        return get("/gov24/v3/serviceDetail", builder -> builder
                .queryParam("page", 1)
                .queryParam("perPage", 1)
                .queryParam("serviceKey", properties.serviceKey())
                .queryParam("cond[서비스ID::EQ]", serviceId));
    }

    public String fetchSupportConditions(String serviceId) {
        return get("/gov24/v3/supportConditions", builder -> builder
                .queryParam("page", 1)
                .queryParam("perPage", 1)
                .queryParam("serviceKey", properties.serviceKey())
                .queryParam("cond[서비스ID::EQ]", serviceId));
    }

    private String get(String path, UriCustomizer customizer) {
        if (!properties.hasServiceKey()) {
            throw new PublicDataClientException("PUBLIC_DATA_SERVICE_KEY is required for Gov24 policy API", null);
        }
        try {
            return restClient.get()
                    .uri(uriBuilder -> customizer.customize(uriBuilder.path(path)).build())
                    .retrieve()
                    .body(String.class);
        } catch (RuntimeException ex) {
            throw new PublicDataClientException("Failed to call Gov24 policy API: " + path, ex);
        }
    }

    @FunctionalInterface
    private interface UriCustomizer {
        UriBuilder customize(UriBuilder builder);
    }
}
