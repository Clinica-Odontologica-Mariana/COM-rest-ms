package com.clinica.mariana.restms.certificate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate")
public class CertificateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "patient_id", nullable = false)
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

	public UUID getId() {
		return id;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public void setPatientId(UUID patientId) {
		this.patientId = patientId;
	}

	public UUID getProfessionalId() {
		return professionalId;
	}

	public void setProfessionalId(UUID professionalId) {
		this.professionalId = professionalId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCertificateType() {
		return certificateType;
	}

	public void setCertificateType(String certificateType) {
		this.certificateType = certificateType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public OffsetDateTime getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(OffsetDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}

	public UUID getStoredFileId() {
		return storedFileId;
	}

	public void setStoredFileId(UUID storedFileId) {
		this.storedFileId = storedFileId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(OffsetDateTime revokedAt) {
		this.revokedAt = revokedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
