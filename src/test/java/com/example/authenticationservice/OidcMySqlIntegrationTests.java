package com.example.authenticationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OidcMySqlIntegrationTests {
    private static final String CLIENT_ID = "integration-client";
    private static final String CLIENT_SECRET = "integration-secret-that-is-not-used-outside-tests";
    private static final String BROWSER_CLIENT_ID = "integration-browser";
    private static final String BROWSER_CLIENT_SECRET = "integration-browser-secret-that-is-not-used-outside-tests";
    private static final Path JWK_SET = createJwkSet();

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("auth_service")
            .withUsername("auth_user")
            .withPassword("container-only-password");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("auth.signing.jwk-set-location", () -> JWK_SET.toUri().toString());
        registry.add("auth.issuer", () -> "http://localhost:8081");
        registry.add("auth.machine-client.enabled", () -> "true");
        registry.add("auth.machine-client.client-id", () -> CLIENT_ID);
        registry.add("auth.machine-client.client-secret", () -> CLIENT_SECRET);
        registry.add("auth.bootstrap-client.enabled", () -> "true");
        registry.add("auth.bootstrap-client.client-id", () -> BROWSER_CLIENT_ID);
        registry.add("auth.bootstrap-client.client-secret", () -> BROWSER_CLIENT_SECRET);
        registry.add("auth.bootstrap-client.redirect-uri", () -> "http://127.0.0.1:3000/api/auth/callback");
        registry.add("auth.bootstrap-client.post-logout-redirect-uri", () -> "http://127.0.0.1:3000/");
    }

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegisteredClientRepository clients;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void persistsConfidentialPkceBrowserClient() {
        RegisteredClient client = clients.findByClientId(BROWSER_CLIENT_ID);

        assertThat(client).isNotNull();
        assertThat(client.getClientAuthenticationMethods())
                .containsExactly(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        assertThat(client.getAuthorizationGrantTypes())
                .containsExactlyInAnyOrder(AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN);
        assertThat(client.getScopes()).containsExactlyInAnyOrder(
                OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL);
        assertThat(client.getClientSettings().isRequireProofKey()).isTrue();
        assertThat(client.getClientSettings().isRequireAuthorizationConsent()).isTrue();
        assertThat(passwordEncoder.matches(BROWSER_CLIENT_SECRET, client.getClientSecret())).isTrue();
        assertThat(client.getClientSecret()).doesNotContain(BROWSER_CLIENT_SECRET);
    }

    @Test
    void registrationUsesProblemDetailsForValidationAndDuplicates() {
        ResponseEntity<JsonNode> invalid = http.postForEntity(
                "/api/v1/auth/registrations",
                Map.of("email", "not-an-email", "password", "short"),
                JsonNode.class);
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalid.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(invalid.getBody().path("code").asText()).isEqualTo("REQUEST_VALIDATION_FAILED");

        Map<String, String> request = Map.of(
                "email", "duplicate@example.test",
                "password", "correct-horse-battery-staple");
        assertThat(http.postForEntity("/api/v1/auth/registrations", request, JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> duplicate = http.postForEntity(
                "/api/v1/auth/registrations", request, JsonNode.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(duplicate.getBody().path("code").asText()).isEqualTo("AUTH_ACCOUNT_EXISTS");
    }

    @Test
    void runsFlywayRegistersUserPublishesDiscoveryAndIssuesVerifiableToken() throws Exception {
        ResponseEntity<String> discovery = http.getForEntity(
                "/.well-known/openid-configuration", String.class);
        assertThat(discovery.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode metadata = objectMapper.readTree(discovery.getBody());
        assertThat(metadata.path("issuer").asText()).isEqualTo("http://localhost:8081");

        ResponseEntity<JsonNode> registration = http.postForEntity(
                "/api/v1/auth/registrations",
                Map.of("email", "integration@example.test", "password", "correct-horse-battery-staple"),
                JsonNode.class);
        assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registration.getBody().path("email").asText()).isEqualTo("integration@example.test");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(CLIENT_ID, CLIENT_SECRET);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", "catalog.read payments.write");

        ResponseEntity<JsonNode> tokenResponse = http.exchange(
                "/oauth2/token", HttpMethod.POST, new HttpEntity<>(form, headers), JsonNode.class);
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String accessToken = tokenResponse.getBody().path("access_token").asText();
        SignedJWT jwt = SignedJWT.parse(accessToken);
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo("http://localhost:8081");
        assertThat(jwt.getJWTClaimsSet().getAudience()).containsExactly("platform-api");
        assertThat(jwt.getJWTClaimsSet().getStringListClaim("scope"))
                .contains("catalog.read", "payments.write");

        ResponseEntity<String> jwks = http.getForEntity("/oauth2/jwks", String.class);
        JWKSet publicKeys = JWKSet.parse(jwks.getBody());
        RSAKey publicKey = publicKeys.getKeyByKeyId(jwt.getHeader().getKeyID()).toRSAKey();
        assertThat(jwt.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(publicKey))).isTrue();
    }

    private static Path createJwkSet() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                    .privateKey((RSAPrivateKey) pair.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID("integration-test-key")
                    .build();
            Path path = Files.createTempFile("auth-integration-", ".jwk.json");
            Files.writeString(path, new JWKSet(rsaKey).toString(false));
            path.toFile().deleteOnExit();
            return path;
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
