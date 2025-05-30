package com.finsight.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    private Auth0 auth0 = new Auth0();
    private Cors cors = new Cors();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class Auth0 {
        @NotBlank
        private String issuerUri;
        
        @NotBlank
        private String audience;
    }

    @Data
    public static class Cors {
        @NotBlank
        private String allowedOrigins;
    }

    @Data
    public static class RateLimit {
        @Positive
        private int requestsPerWindow = 200;
        
        @Positive
        private int windowMinutes = 5;
    }
} 