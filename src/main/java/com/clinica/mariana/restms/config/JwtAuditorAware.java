package com.clinica.mariana.restms.config;

import com.clinica.mariana.restms.users.repository.AppUserReferenceRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("jwtAuditorAware")
public class JwtAuditorAware implements AuditorAware<UUID> {

	private final AppUserReferenceRepository appUserReferenceRepository;

	public JwtAuditorAware(AppUserReferenceRepository appUserReferenceRepository) {
		this.appUserReferenceRepository = appUserReferenceRepository;
	}

	@Override
	public Optional<UUID> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication instanceof JwtAuthenticationToken jwtToken) {
			String sub = jwtToken.getToken().getSubject();
			if (sub != null && !sub.isBlank()) {
				return resolveAuditorId(sub);
			}
		}

		return Optional.empty();
	}

	private Optional<UUID> resolveAuditorId(String keycloakSubject) {
		try {
			Optional<UUID> userId = appUserReferenceRepository.findActiveByKeycloakSubject(keycloakSubject)
					.map(AppUserReferenceRepository.AppUserReference::id);

			if (userId.isPresent()) {
				return userId;
			}

			UUID subjectAsId = UUID.fromString(keycloakSubject);
			return appUserReferenceRepository.findActiveById(subjectAsId)
					.map(AppUserReferenceRepository.AppUserReference::id);
		} catch (DataAccessException | IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}
}
