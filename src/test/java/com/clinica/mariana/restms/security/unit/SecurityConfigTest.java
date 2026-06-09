package com.clinica.mariana.restms.security.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinica.mariana.restms.security.config.RestAccessDeniedHandler;
import com.clinica.mariana.restms.security.config.RestAuthenticationEntryPoint;
import com.clinica.mariana.restms.security.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityConfigTest {

	private final SecurityConfig securityConfig = new SecurityConfig(
			new RestAuthenticationEntryPoint(new ObjectMapper().findAndRegisterModules()),
			new RestAccessDeniedHandler(new ObjectMapper().findAndRegisterModules()));

	@Test
	void shouldMapRealmRolesToSpringAuthorities() {
		Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("ADMIN", "DOCTOR"))));

		var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

		assertThat(authentication).isNotNull();
		assertThat(authentication.getName()).isEqualTo("api-admin");
		assertThat(authentication.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_ADMIN",
				"ROLE_DOCTOR");
	}

	@Test
	void shouldIgnoreMissingRealmRoles() {
		Jwt jwt = jwt(Map.of());

		var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities()).isEmpty();
	}

	private Jwt jwt(Map<String, Object> extraClaims) {
		return Jwt.withTokenValue("token").header("alg", "none").subject("keycloak-subject")
				.claim("preferred_username", "api-admin").claims(claims -> claims.putAll(extraClaims))
				.issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
	}
}
