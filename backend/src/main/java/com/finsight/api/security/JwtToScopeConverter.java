package com.finsight.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public enum JwtToScopeConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    INSTANCE;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var scopes = jwt.getClaimAsString("scope");
        return scopes == null ? java.util.List.of()
                : java.util.Arrays.stream(scopes.split("\\s+"))
                .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                .collect(Collectors.toSet());
    }
}

