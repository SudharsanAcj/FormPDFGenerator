package com.formgenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "rti")
public class RtiProperties {

    private Map<String, RtiEndpointConfig> endpoints = new HashMap<>();
    private Submission submission = new Submission();

    @Data
    public static class RtiEndpointConfig {
        private String url;
        /** API_KEY or OAUTH2 */
        private String authType = "API_KEY";
        private String apiKey;
        private OAuth2Config oauth2;

        @Data
        public static class OAuth2Config {
            private String tokenUrl;
            private String clientId;
            private String clientSecret;
            private String scope;
        }
    }

    @Data
    public static class Submission {
        private int timeoutSeconds = 45;
        private int maxRetries = 3;
        private long retryDelayMs = 2000;
    }
}
