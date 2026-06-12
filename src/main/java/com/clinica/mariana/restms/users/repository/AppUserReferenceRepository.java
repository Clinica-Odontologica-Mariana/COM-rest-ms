package com.clinica.mariana.restms.users.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AppUserReferenceRepository {

	private final EntityManager entityManager;

	public AppUserReferenceRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Optional<AppUserReference> findActiveByKeycloakSubject(String keycloakSubject) {
		return entityManager
				.createNativeQuery(
						"select id from app_user where keycloak_subject = :keycloakSubject and active = true")
				.setParameter("keycloakSubject", keycloakSubject).setFlushMode(FlushModeType.COMMIT).getResultStream()
				.findFirst().map(value -> new AppUserReference(toUuid(value)));
	}

	public Optional<AppUserReference> findActiveById(UUID id) {
		return entityManager.createNativeQuery("select id from app_user where id = :id and active = true")
				.setParameter("id", id).setFlushMode(FlushModeType.COMMIT).getResultStream().findFirst()
				.map(value -> new AppUserReference(toUuid(value)));
	}

	private UUID toUuid(Object value) {
		if (value instanceof UUID uuid) {
			return uuid;
		}
		if (value instanceof byte[] bytes && bytes.length == 16) {
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			return new UUID(buffer.getLong(), buffer.getLong());
		}
		return UUID.fromString(value.toString());
	}

	public record AppUserReference(UUID id) {
	}
}
