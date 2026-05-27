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
@Table(name = "treatment_plan_item")
public class TreatmentPlanItemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "treatment_plan_id", nullable = false)
	private UUID treatmentPlanId;

	@Column(name = "procedure_id")
	private UUID procedureId;

	@Column(name = "tooth_number")
	private Integer toothNumber;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "estimated_price")
	private BigDecimal estimatedPrice;

	@Column(name = "status", nullable = false, length = 30)
	private String status = "PENDING";

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder = 1;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public UUID getTreatmentPlanId() {
		return treatmentPlanId;
	}

	public void setTreatmentPlanId(UUID treatmentPlanId) {
		this.treatmentPlanId = treatmentPlanId;
	}

	public UUID getProcedureId() {
		return procedureId;
	}

	public void setProcedureId(UUID procedureId) {
		this.procedureId = procedureId;
	}

	public Integer getToothNumber() {
		return toothNumber;
	}

	public void setToothNumber(Integer toothNumber) {
		this.toothNumber = toothNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getEstimatedPrice() {
		return estimatedPrice;
	}

	public void setEstimatedPrice(BigDecimal estimatedPrice) {
		this.estimatedPrice = estimatedPrice;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(OffsetDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
