package com.clinica.mariana.restms.treatment.entity;

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

	@Column(name = "category", length = 100)
	private String category;

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

}
