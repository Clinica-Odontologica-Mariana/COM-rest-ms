package com.clinica.mariana.restms.financial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "financial_transaction")
public class FinancialTransactionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "clinic_id", nullable = false)
	private UUID clinicId;

	@Column(name = "appointment_id")
	private UUID appointmentId;

	@Column(name = "treatment_plan_id")
	private UUID treatmentPlanId;

	@Column(name = "description", nullable = false, length = 255)
	private String description;

	@Column(name = "type", nullable = false, length = 10)
	private String type;

	@Column(name = "category", length = 80)
	private String category;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "status", nullable = false, length = 20)
	private String status = "PENDING";

	@Column(name = "transaction_date", nullable = false)
	private LocalDate transactionDate;

	@Column(name = "notes")
	private String notes;

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;
}
