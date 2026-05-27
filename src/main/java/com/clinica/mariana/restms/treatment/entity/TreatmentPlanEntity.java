package com.clinica.mariana.restms.treatment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "treatment_plan")
public class TreatmentPlanEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "professional_id")
	private UUID professionalId;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "status", nullable = false, length = 30)
	private String status = "DRAFT";

	@Column(name = "notes")
	private String notes;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

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

	public UUID getProfessionalId() {
		return professionalId;
	}

	public void setProfessionalId(UUID professionalId) {
		this.professionalId = professionalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
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

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
