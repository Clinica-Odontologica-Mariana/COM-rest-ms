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
@Table(name = "medical_record")
public class MedicalRecordEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false, unique = true)
	private UUID patientId;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "allergies")
	private String allergies;

	@Column(name = "chronic_conditions")
	private String chronicConditions;

	@Column(name = "continuous_medications")
	private String continuousMedications;

	@Column(name = "general_observations")
	private String generalObservations;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getAllergies() {
		return allergies;
	}

	public void setAllergies(String allergies) {
		this.allergies = allergies;
	}

	public String getChronicConditions() {
		return chronicConditions;
	}

	public void setChronicConditions(String chronicConditions) {
		this.chronicConditions = chronicConditions;
	}

	public String getContinuousMedications() {
		return continuousMedications;
	}

	public void setContinuousMedications(String continuousMedications) {
		this.continuousMedications = continuousMedications;
	}

	public String getGeneralObservations() {
		return generalObservations;
	}

	public void setGeneralObservations(String generalObservations) {
		this.generalObservations = generalObservations;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
