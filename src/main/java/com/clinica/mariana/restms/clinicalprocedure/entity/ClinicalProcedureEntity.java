package com.clinica.mariana.restms.clinicalprocedure.entity;

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

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getEstimatedDurationMinutes() {
		return estimatedDurationMinutes;
	}

	public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
		this.estimatedDurationMinutes = estimatedDurationMinutes;
	}

	public BigDecimal getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(BigDecimal basePrice) {
		this.basePrice = basePrice;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getInactivatedAt() {
		return inactivatedAt;
	}

	public void setInactivatedAt(OffsetDateTime inactivatedAt) {
		this.inactivatedAt = inactivatedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
