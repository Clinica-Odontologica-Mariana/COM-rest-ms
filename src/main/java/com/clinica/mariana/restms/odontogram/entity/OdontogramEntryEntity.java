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
@Table(name = "odontogram_entry")
public class OdontogramEntryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "tooth_number", nullable = false)
	private Integer toothNumber;

	@Column(name = "surface_code", length = 20)
	private String surfaceCode;

	@Column(name = "condition_code", nullable = false, length = 50)
	private String conditionCode;

	@Column(name = "notes")
	private String notes;

	@Column(name = "recorded_by_professional_id")
	private UUID recordedByProfessionalId;

	@Column(name = "recorded_at", insertable = false, updatable = false)
	private OffsetDateTime recordedAt;

	public UUID getId() {
		return id;
	}

	public UUID getMedicalRecordId() {
		return medicalRecordId;
	}

	public void setMedicalRecordId(UUID medicalRecordId) {
		this.medicalRecordId = medicalRecordId;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public Integer getToothNumber() {
		return toothNumber;
	}

	public void setToothNumber(Integer toothNumber) {
		this.toothNumber = toothNumber;
	}

	public String getSurfaceCode() {
		return surfaceCode;
	}

	public void setSurfaceCode(String surfaceCode) {
		this.surfaceCode = surfaceCode;
	}

	public String getConditionCode() {
		return conditionCode;
	}

	public void setConditionCode(String conditionCode) {
		this.conditionCode = conditionCode;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public UUID getRecordedByProfessionalId() {
		return recordedByProfessionalId;
	}

	public void setRecordedByProfessionalId(UUID recordedByProfessionalId) {
		this.recordedByProfessionalId = recordedByProfessionalId;
	}

	public OffsetDateTime getRecordedAt() {
		return recordedAt;
	}
}
