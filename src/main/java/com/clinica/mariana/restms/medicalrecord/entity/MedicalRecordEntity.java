package com.clinica.mariana.restms.medicalrecord.entity;

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

import java.time.OffsetDateTime;
import java.util.UUID;

@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@Setter
@Entity
@Table(name = "medical_record")
public class MedicalRecordEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false, unique = true)
	private UUID patientId;

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@Column(name = "allergies")
	private String allergies;

	@Column(name = "chronic_conditions")
	private String chronicConditions;

	@Column(name = "continuous_medications")
	private String continuousMedications;

	@Column(name = "general_observations")
	private String generalObservations;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
