package com.example.authenticationservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "auth.bootstrap-client.enabled", havingValue = "true")
public class OAuthClientBootstrap implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder passwordEncoder;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String postLogoutRedirectUri;

    public OAuthClientBootstrap(
            RegisteredClientRepository clients,
            PasswordEncoder passwordEncoder,
            @Value("${auth.bootstrap-client.client-id}") String clientId,
            @Value("${auth.bootstrap-client.client-secret}") String clientSecret,
            @Value("${auth.bootstrap-client.redirect-uri}") String redirectUri,
            @Value("${auth.bootstrap-client.post-logout-redirect-uri}") String postLogoutRedirectUri) {
        this.clients = clients;
        this.passwordEncoder = passwordEncoder;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (clients.findByClientId(clientId) != null) {
            return;
        }
        if (!StringUtils.hasText(clientSecret)) {
            throw new IllegalStateException("AUTH_OIDC_CLIENT_SECRET is required when client bootstrap is enabled");
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.nameUUIDFromBytes(clientId.getBytes(StandardCharsets.UTF_8)).toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName("Platform browser BFF")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .postLogoutRedirectUri(postLogoutRedirectUri)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .requireProofKey(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .refreshTokenTimeToLive(Duration.ofHours(8))
                        .reuseRefreshTokens(false)
                        .build())
                .build();
        clients.save(client);
    }
}
