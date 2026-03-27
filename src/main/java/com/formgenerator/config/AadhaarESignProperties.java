package com.formgenerator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "esign")
public class AadhaarESignProperties {

    private Gateway gateway = new Gateway();
    private Asp asp = new Asp();

    @Data
    public static class Gateway {
        private String url;
        private String responseUrl;
        private String version = "2.1";
        private String certPath;
        private int timeoutSeconds = 30;
    }

    @Data
    public static class Asp {
        private String id;
        private String keystorePath;
        private String keystorePassword;
        private String keyAlias;
        /** OTP=1, BIOMETRIC=2, IRIS=3 */
        private String authMode = "1";
    }
}
