package com.clinica.mariana.restms.certificate.entity;

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
@Table(name = "certificate")
public class CertificateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id")
	private UUID patientId;

	@Column(name = "professional_id")
	private UUID professionalId;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "certificate_type", nullable = false, length = 50)
	private String certificateType = "ATTENDANCE";

	@Column(name = "content")
	private String content;

	@Column(name = "issued_at", nullable = false)
	private OffsetDateTime issuedAt;

	@Column(name = "stored_file_id")
	private UUID storedFileId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@Column(name = "revoked_at")
	private OffsetDateTime revokedAt;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

}
