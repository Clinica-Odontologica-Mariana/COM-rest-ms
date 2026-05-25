package com.clinica.mariana.restms.storedfile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile_photo")
public class UserProfilePhotoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "stored_file_id", nullable = false, unique = true)
	private UUID storedFileId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getStoredFileId() {
		return storedFileId;
	}

	public void setStoredFileId(UUID storedFileId) {
		this.storedFileId = storedFileId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
