package com.clinica.mariana.restms.odontogram.entity;

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
@Table(name = "odontogram_entry")
public class OdontogramEntryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "medical_record_id", nullable = false)
	private UUID medicalRecordId;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "tooth_number", nullable = false)
	private Integer toothNumber;

	@Column(name = "surface_code", length = 20)
	private String surfaceCode;

	@Column(name = "condition_code", nullable = false, length = 50)
	private String conditionCode;

	@Column(name = "notes")
	private String notes;

	@Column(name = "recorded_by_professional_id")
	private UUID recordedByProfessionalId;

	@Column(name = "recorded_at", insertable = false, updatable = false)
	private OffsetDateTime recordedAt;

}
