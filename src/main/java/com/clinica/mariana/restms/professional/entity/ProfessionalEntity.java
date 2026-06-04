package com.clinica.mariana.restms.professional.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "professional")
public class ProfessionalEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false, unique = true)
	private UUID userId;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	@Column(name = "specialty_id", nullable = false)
	private UUID specialtyId;

	@Column(name = "license_number", nullable = false, length = 50)
	private String licenseNumber;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "inactivated_at")
	private OffsetDateTime inactivatedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
