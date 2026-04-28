package com.company.itoms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.api")
public class AiConfigProperties {
    private boolean enabled = true;
    private String url;
    private String accessKey;
    private String secretKey;
    private int timeout = 5000;
    private int retryCount = 3;
    private long retryDelay = 1000;
}