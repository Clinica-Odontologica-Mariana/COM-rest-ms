package com.clinica.mariana.restms.medicalrecord.entity;

import com.clinica.mariana.restms.patient.entity.PatientEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_record")
public class MedicalRecordEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "patient_id", nullable = false, unique = true)
	private PatientEntity patient;

	@Column(name = "allergies", columnDefinition = "TEXT")
	private String allergies;

	@Column(name = "chronic_conditions", columnDefinition = "TEXT")
	private String chronicConditions;

	@Column(name = "continuous_medications", columnDefinition = "TEXT")
	private String continuousMedications;

	@Column(name = "general_observations", columnDefinition = "TEXT")
	private String generalObservations;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public PatientEntity getPatient() {
		return patient;
	}

	public void setPatient(PatientEntity patient) {
		this.patient = patient;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
