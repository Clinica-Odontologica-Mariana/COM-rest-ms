package com.clinica.mariana.restms.odontogram.entity;

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
@Table(name = "odontogram_file")
public class OdontogramFileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
	private UUID patientId;

	@Column(name = "medical_record_id")
	private UUID medicalRecordId;

	@Column(name = "odontogram_entry_id")
	private UUID odontogramEntryId;

	@Column(name = "stored_file_id", nullable = false, unique = true)
	private UUID storedFileId;

	@Column(name = "description")
	private String description;

	@CreatedBy
	@Column(name = "created_by_user_id", updatable = false)
	private UUID createdByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

}
