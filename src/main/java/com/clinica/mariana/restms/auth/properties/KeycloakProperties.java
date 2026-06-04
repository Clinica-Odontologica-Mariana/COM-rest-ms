package com.clinica.mariana.restms.auth.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(@NotBlank(message = "KEYCLOAK_BASE_URL is required") String baseUrl,

		@NotBlank(message = "KEYCLOAK_REALM is required") String realm,

		@NotBlank(message = "KEYCLOAK_CLIENT_ID is required") String clientId,

		@NotBlank(message = "KEYCLOAK_CLIENT_SECRET is required") String clientSecret,

		@NotBlank(message = "KEYCLOAK_ADMIN_USERNAME is required") String adminUsername,

		@NotBlank(message = "KEYCLOAK_ADMIN_PASSWORD is required") String adminPassword) {
}
