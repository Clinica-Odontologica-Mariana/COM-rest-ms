package com.clinica.mariana.restms.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("jwtAuditorAware")
public class JwtAuditorAware implements AuditorAware<UUID> {

	@Override
	public Optional<UUID> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtToken) {
			String sub = jwtToken.getToken().getSubject();
			if (sub != null && !sub.isBlank()) {
				try {
					return Optional.of(UUID.fromString(sub));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}

		return Optional.empty();
	}
}
