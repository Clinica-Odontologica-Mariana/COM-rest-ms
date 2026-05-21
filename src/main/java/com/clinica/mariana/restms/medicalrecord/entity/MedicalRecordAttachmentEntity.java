package com.clinica.mariana.restms.medicalrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_record_attachment")
public class MedicalRecordAttachmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "stored_file_id", nullable = false, unique = true)
	private UUID storedFileId;

	@Column(name = "description")
	private String description;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getMedicalRecordId() {
		return medicalRecordId;
	}

	public void setMedicalRecordId(UUID medicalRecordId) {
		this.medicalRecordId = medicalRecordId;
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

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
