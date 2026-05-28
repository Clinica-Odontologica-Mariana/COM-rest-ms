package com.clinica.mariana.restms.service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "service")
public class ServiceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private ServiceCategoryEntity category;

	@Column(name = "created_by_user_id")
	private UUID createdByUserId;

	@Column(name = "name", nullable = false, length = 150)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "estimated_duration_minutes")
	private Integer estimatedDurationMinutes;

	@Column(name = "active", nullable = false)
	private boolean active;

	@Column(name = "inactivated_at")
	private OffsetDateTime inactivatedAt;

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

	public ServiceCategoryEntity getCategory() {
		return category;
	}

	public void setCategory(ServiceCategoryEntity category) {
		this.category = category;
	}

	public UUID getCreatedByUserId() {
		return createdByUserId;
	}

	public void setCreatedByUserId(UUID createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
