package com.clinica.mariana.restms.storedfile.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class StoredFileReferenceRepository {

	private final EntityManager entityManager;

	public StoredFileReferenceRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public boolean patientExists(UUID id) {
		return existsById("patient", id);
	}

	public boolean medicalRecordExists(UUID id) {
		return existsById("medical_record", id);
	}

	public boolean odontogramEntryExists(UUID id) {
		return existsById("odontogram_entry", id);
	}

	private boolean existsById(String tableName, UUID id) {
		Number count = (Number) entityManager.createNativeQuery("select count(*) from " + tableName + " where id = :id")
				.setParameter("id", id).getSingleResult();
		return count.longValue() > 0;
	}
}
