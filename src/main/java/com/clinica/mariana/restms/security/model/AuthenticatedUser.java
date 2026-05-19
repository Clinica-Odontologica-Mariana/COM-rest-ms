package com.clinica.mariana.restms.security.model;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record AuthenticatedUser(
		String subject,
		String username,
		String email,
		Set<String> roles,
		Map<String, Object> claims
) {
	public static AuthenticatedUser fromJwt(Jwt jwt) {
		return new AuthenticatedUser(
				jwt.getSubject(),
				jwt.getClaimAsString("preferred_username"),
				jwt.getClaimAsString("email"),
				extractRoles(jwt.getClaims()),
				jwt.getClaims()
		);
	}

	private static Set<String> extractRoles(Map<String, Object> claims) {
		Object realmAccessObject = claims.get("realm_access");
		if (!(realmAccessObject instanceof Map<?, ?> realmAccess)) {
			return Set.of();
		}

		Object rolesObject = realmAccess.get("roles");
		if (!(rolesObject instanceof Collection<?> rolesCollection)) {
			return Set.of();
		}

		Set<String> roles = new LinkedHashSet<>();
		for (Object role : rolesCollection) {
			if (role instanceof String roleName && !roleName.isBlank()) {
				roles.add(roleName);
			}
		}
		return Set.copyOf(roles);
	}
}
