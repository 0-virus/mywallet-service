package com.ragvirus.policy.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragvirus.policy.domain.PolicySourceRaw;
import com.ragvirus.policy.repository.PolicySourceRawRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicySourceRawService {

    private final PolicySourceRawRepository repository;
    private final ObjectMapper objectMapper;

    public PolicySourceRawService(PolicySourceRawRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveJson(String sourceType, String sourceServiceId, String endpoint, String rawJson) {
        save(sourceType, sourceServiceId, endpoint, normalizeJson(rawJson));
    }

    @Transactional
    public void saveXml(String sourceType, String sourceServiceId, String endpoint, String rawXml) {
        try {
            String wrapped = objectMapper.writeValueAsString(new RawXml(rawXml));
            save(sourceType, sourceServiceId, endpoint, wrapped);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Failed to wrap XML raw response as JSON", ex);
        }
    }

    private void save(String sourceType, String sourceServiceId, String endpoint, String rawData) {
        repository.findBySourceTypeAndSourceServiceIdAndEndpoint(sourceType, sourceServiceId, endpoint)
                .ifPresentOrElse(
                        sourceRaw -> sourceRaw.updateRawData(rawData),
                        () -> repository.save(new PolicySourceRaw(sourceType, sourceServiceId, endpoint, rawData))
                );
    }

    private String normalizeJson(String rawJson) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(rawJson));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Raw response is not valid JSON", ex);
        }
    }

    private record RawXml(String xml) {
    }
}
