package com.finsight.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;

import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String ISSUER_URI = "https://dev-ugadnr0ui0vziqee.us.auth0.com/";
    // Auth0 API Identifier
    private static final String AUDIENCE = "https://finsight-api";

    /**
     * Defines HTTP security:
     *  - Disables CSRF
     *  - Secures /api/** endpoints
     *  - Uses OAuth2 Resource Server JWT support with a custom decoder
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.decoder(jwtDecoder()).jwtAuthenticationConverter(jwtAuthConverter()))
                );
        return http.build();
    }

    /**
     * Converts JWT scopes/permissions into Spring Security authorities.
     */
    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("ROLE_");
        scopesConverter.setAuthoritiesClaimName("permissions");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(scopesConverter);
        return converter;
    }

    /**
     * Configures a NimbusJwtDecoder that:
     *  1. Validates the token's issuer (iss)
     *  2. Validates the token's audience (aud)
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use issuer URI to fetch JWK set and default validators :contentReference[oaicite:0]{index=0}
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
                JwtDecoders.fromOidcIssuerLocation(ISSUER_URI);

        // Validator for the "aud" claim: must contain our API identifier: contentReference[oaicite:1]{index=1}
        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>("aud", aud -> aud.contains(AUDIENCE));

        // Default validator for issuer, exp, nbf, etc. :contentReference[oaicite:2]{index=2}
        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(ISSUER_URI);

        // Chain issuer + audience validators :contentReference[oaicite:3]{index=3}
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
    }
}
