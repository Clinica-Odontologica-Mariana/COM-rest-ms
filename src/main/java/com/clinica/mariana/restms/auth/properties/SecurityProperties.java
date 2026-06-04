package com.clinica.mariana.restms.auth.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.jwt")
public record SecurityProperties(
		@NotBlank(message = "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI is required") String issuerUri) {
}
