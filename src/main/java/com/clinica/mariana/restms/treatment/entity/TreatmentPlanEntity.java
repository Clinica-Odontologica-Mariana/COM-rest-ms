package com.clinica.mariana.restms.treatment.entity;

import lombok.Getter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedBy;
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

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "treatment_plan")
public class TreatmentPlanEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "professional_id")
	private UUID professionalId;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "status", nullable = false, length = 30)
	private String status = "DRAFT";

	@Column(name = "notes")
	private String notes;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
