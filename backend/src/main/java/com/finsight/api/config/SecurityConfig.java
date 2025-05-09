package com.finsight.api.config;

import com.finsight.api.security.JwtToScopeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    private static final String ISSUER_URI = "https://dev-ugadnr0ui0vziqee.us.auth0.com/";
    private static final String AUDIENCE   = "https://finsight-api";

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {

        /* 1) Build a JwtAuthenticationConverter that uses our scope converter */
        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(JwtToScopeConverter.INSTANCE);

        /* 2) Configure the filter chain */
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.setAllowedOrigins(List.of("https://app.finsight.com"));
                    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    cfg.setAllowedHeaders(List.of("Authorization","Content-Type"));
                    cfg.setMaxAge(Duration.ofHours(1));
                    return cfg;
                }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()     // pre‑flight
                        .requestMatchers("/api/**").hasAuthority("SCOPE_fin:app")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())               // strict claim checks
                                .jwtAuthenticationConverter(jwtAuthConverter)));  // ✓ no cast
        return http.build();
    }

    /** Validates iss, exp/nbf, aud *and* azp */
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder =
                (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(ISSUER_URI);

        OAuth2TokenValidator<Jwt> aud = new JwtClaimValidator<List<String>>(
                "aud", list -> list.contains(AUDIENCE));

        OAuth2TokenValidator<Jwt> azp = new JwtClaimValidator<>(
                "azp", Predicate.isEqual(AUDIENCE));

        OAuth2TokenValidator<Jwt> issuer = JwtValidators.createDefaultWithIssuer(ISSUER_URI);

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuer, aud, azp));
        return decoder;
    }
}
