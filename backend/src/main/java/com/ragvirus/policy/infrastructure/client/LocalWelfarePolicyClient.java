package com.ragvirus.policy.infrastructure.client;

import com.ragvirus.policy.config.PolicyDataProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LocalWelfarePolicyClient {

    private final PolicyDataProperties properties;
    private final RestClient restClient;

    public LocalWelfarePolicyClient(PolicyDataProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.localWelfareBaseUrl()).build();
    }

    public String fetchWelfareList(int pageNo, int numOfRows, String ctpvNm, String sggNm, String age) {
        return getList(pageNo, numOfRows, ctpvNm, sggNm, age);
    }

    public String fetchWelfareDetail(String servId) {
        if (!properties.hasServiceKey()) {
            throw new PublicDataClientException("PUBLIC_DATA_SERVICE_KEY is required for local welfare policy API", null);
        }
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/LcgvWelfaredetailed")
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("servId", servId)
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (RuntimeException ex) {
            throw new PublicDataClientException("Failed to call local welfare detail API", ex);
        }
    }

    private String getList(int pageNo, int numOfRows, String ctpvNm, String sggNm, String age) {
        if (!properties.hasServiceKey()) {
            throw new PublicDataClientException("PUBLIC_DATA_SERVICE_KEY is required for local welfare policy API", null);
        }
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/LcgvWelfarelist")
                                .queryParam("serviceKey", properties.serviceKey())
                                .queryParam("pageNo", pageNo)
                                .queryParam("numOfRows", numOfRows);
                        if (ctpvNm != null && !ctpvNm.isBlank()) {
                            uriBuilder.queryParam("ctpvNm", ctpvNm);
                        }
                        if (sggNm != null && !sggNm.isBlank()) {
                            uriBuilder.queryParam("sggNm", sggNm);
                        }
                        if (age != null && !age.isBlank()) {
                            uriBuilder.queryParam("age", age);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(String.class);
        } catch (RuntimeException ex) {
            throw new PublicDataClientException("Failed to call local welfare list API", ex);
        }
    }
}
