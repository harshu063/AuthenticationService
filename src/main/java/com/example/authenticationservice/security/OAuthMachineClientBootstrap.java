package com.example.authenticationservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "auth.machine-client.enabled", havingValue = "true")
public class OAuthMachineClientBootstrap implements ApplicationRunner {
    private final RegisteredClientRepository clients;
    private final PasswordEncoder passwordEncoder;
    private final String clientId;
    private final String clientSecret;

    public OAuthMachineClientBootstrap(
            RegisteredClientRepository clients,
            PasswordEncoder passwordEncoder,
            @Value("${auth.machine-client.client-id}") String clientId,
            @Value("${auth.machine-client.client-secret}") String clientSecret) {
        this.clients = clients;
        this.passwordEncoder = passwordEncoder;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (clients.findByClientId(clientId) != null) {
            return;
        }
        if (!StringUtils.hasText(clientSecret)) {
            throw new IllegalStateException(
                    "AUTH_MACHINE_CLIENT_SECRET is required when machine-client bootstrap is enabled");
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.nameUUIDFromBytes(clientId.getBytes(StandardCharsets.UTF_8)).toString())
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(clientSecret))
                .clientName("Local platform integration client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("catalog.read")
                .scope("catalog.write")
                .scope("payments.write")
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(5))
                        .build())
                .build();
        clients.save(client);
    }
}
