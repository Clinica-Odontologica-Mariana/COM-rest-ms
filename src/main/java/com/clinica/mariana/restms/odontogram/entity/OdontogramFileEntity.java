package com.clinica.mariana.restms.odontogram.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "odontogram_file")
public class OdontogramFileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "medical_record_id")
	private UUID medicalRecordId;

	@Column(name = "odontogram_entry_id")
	private UUID odontogramEntryId;

	@Column(name = "stored_file_id", nullable = false, unique = true)
	private UUID storedFileId;

	@Column(name = "description")
	private String description;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public UUID getMedicalRecordId() {
		return medicalRecordId;
	}

	public void setMedicalRecordId(UUID medicalRecordId) {
		this.medicalRecordId = medicalRecordId;
	}

	public UUID getOdontogramEntryId() {
		return odontogramEntryId;
	}

	public void setOdontogramEntryId(UUID odontogramEntryId) {
		this.odontogramEntryId = odontogramEntryId;
	}

	public UUID getStoredFileId() {
		return storedFileId;
	}

	public void setStoredFileId(UUID storedFileId) {
		this.storedFileId = storedFileId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
