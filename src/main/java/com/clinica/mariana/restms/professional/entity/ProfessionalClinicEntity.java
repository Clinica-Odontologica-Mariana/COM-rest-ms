package com.clinica.mariana.restms.professional.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "professional_clinic")
public class ProfessionalClinicEntity {

	@EmbeddedId
	private ProfessionalClinicId id;

	@Column(name = "primary_clinic", nullable = false)
	private boolean primaryClinic;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public ProfessionalClinicId getId() {
		return id;
	}

	public void setId(ProfessionalClinicId id) {
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
