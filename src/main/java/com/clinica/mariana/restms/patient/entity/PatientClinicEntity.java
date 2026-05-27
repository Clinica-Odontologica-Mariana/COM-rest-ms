package com.clinica.mariana.restms.patient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "patient_clinic")
public class PatientClinicEntity {

	@EmbeddedId
	private PatientClinicId id;

	@Column(name = "primary_clinic", nullable = false)
	private boolean primaryClinic;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public PatientClinicId getId() {
		return id;
	}

	public void setId(PatientClinicId id) {
		this.id = id;
	}

	public boolean isPrimaryClinic() {
		return primaryClinic;
	}

	public void setPrimaryClinic(boolean primaryClinic) {
		this.primaryClinic = primaryClinic;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
