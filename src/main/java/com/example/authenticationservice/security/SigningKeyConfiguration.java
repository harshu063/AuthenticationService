package com.example.authenticationservice.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SigningKeyConfiguration {

    @Bean
    JWKSource<SecurityContext> jwkSource(
            @Value("${auth.signing.jwk-set-location}") Resource jwkSetResource) throws Exception {
        if (!jwkSetResource.exists() || !jwkSetResource.isReadable()) {
            throw new IllegalStateException("OIDC signing JWK set is missing or unreadable");
        }

        String json;
        try (var input = jwkSetResource.getInputStream()) {
            json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        JWKSet jwkSet = JWKSet.parse(json);
        List<JWK> signingKeys = jwkSet.getKeys().stream()
                .filter(key -> key instanceof RSAKey rsaKey && rsaKey.isPrivate() && key.getKeyID() != null)
                .toList();
        if (signingKeys.isEmpty()) {
            throw new IllegalStateException("OIDC signing JWK set must contain an RSA private key with a key ID");
        }

        return new ImmutableJWKSet<>(jwkSet);
    }
}
