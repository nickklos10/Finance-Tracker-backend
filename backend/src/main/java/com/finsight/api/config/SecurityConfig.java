package com.finsight.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity          // harmless
public class SecurityConfig {

    /** Auth0 tenant’s issuer URL (keep trailing slash) */
    private static final String ISSUER_URI = "https://dev-ugadnr0ui0vziqee.us.auth0.com/";
    /** API Identifier set in Auth0 → APIs */
    private static final String AUDIENCE   = "https://finsight-api";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()   // JWT required
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.decoder(jwtDecoder())));
        return http.build();
    }

    /** Validating issuer AND audience; no scope mapping. */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder =
                (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(ISSUER_URI);

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>("aud", aud -> aud.contains(AUDIENCE));

        OAuth2TokenValidator<Jwt> withIssuer =
                JwtValidators.createDefaultWithIssuer(ISSUER_URI);

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
        return decoder;
    }
}
