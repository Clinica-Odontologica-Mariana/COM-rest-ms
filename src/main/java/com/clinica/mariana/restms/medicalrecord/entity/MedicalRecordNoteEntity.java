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
@Table(name = "medical_record_note")
public class MedicalRecordNoteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "note", nullable = false)
	private String note;

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

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
