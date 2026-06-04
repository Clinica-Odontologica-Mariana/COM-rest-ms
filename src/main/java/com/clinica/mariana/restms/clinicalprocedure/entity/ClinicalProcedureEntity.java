package com.clinica.mariana.restms.clinicalprocedure.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "clinical_procedure")
public class ClinicalProcedureEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "code", length = 50, unique = true)
	private String code;

	@Column(name = "name", nullable = false, length = 150, unique = true)
	private String name;

	@Column(name = "category", length = 80)
	private String category;

	@Column(name = "description")
	private String description;

	@Column(name = "estimated_duration_minutes")
	private Integer estimatedDurationMinutes;

	@Column(name = "base_price")
	private BigDecimal basePrice;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "inactivated_at")
	private OffsetDateTime inactivatedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
