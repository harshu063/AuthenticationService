package com.example.authenticationservice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthConfigurationSecurityTests {

    @Test
    void databasePasswordHasNoCommittedFallback() throws IOException {
        Properties properties = loadApplicationProperties();

        assertEquals("\u0024{AUTH_DB_PASSWORD}", properties.getProperty("spring.datasource.password"));
    }

    @Test
    void optionalClientSecretsHaveOnlyEmptyFallbacks() throws IOException {
        Properties properties = loadApplicationProperties();

        assertEquals("\u0024{AUTH_OIDC_CLIENT_SECRET:}",
                properties.getProperty("auth.bootstrap-client.client-secret"));
        assertEquals("\u0024{AUTH_MACHINE_CLIENT_SECRET:}",
                properties.getProperty("auth.machine-client.client-secret"));
    }

    private Properties loadApplicationProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IOException("application.properties is missing from the test classpath");
            }
            properties.load(input);
        }
        return properties;
    }
}
