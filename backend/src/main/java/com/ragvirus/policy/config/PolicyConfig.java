package com.ragvirus.policy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PolicyDataProperties.class, PolicyOpenAiProperties.class})
public class PolicyConfig {
}
