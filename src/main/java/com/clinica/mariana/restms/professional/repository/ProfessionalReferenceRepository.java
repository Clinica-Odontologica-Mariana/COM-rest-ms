package com.clinica.mariana.restms.professional.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ProfessionalReferenceRepository {

	private final EntityManager entityManager;

	public ProfessionalReferenceRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public boolean userExists(UUID id) {
		return existsById("app_user", id);
	}

	public boolean clinicExists(UUID id) {
		return existsById("clinic", id);
	}

	public boolean specialtyExists(UUID id) {
		return existsById("specialty", id);
	}

	private boolean existsById(String tableName, UUID id) {
		Number count = (Number) entityManager
				.createNativeQuery("select count(*) from " + tableName + " where id = :id")
				.setParameter("id", id)
				.getSingleResult();
		return count.longValue() > 0;
	}
}
