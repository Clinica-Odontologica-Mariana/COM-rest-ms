package com.clinica.mariana.restms.professional.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Setter
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

}
