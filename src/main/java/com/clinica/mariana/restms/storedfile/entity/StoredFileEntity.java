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
@Table(name = "stored_file")
public class StoredFileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "bucket_name", nullable = false, length = 100)
	private String bucketName;

	@Column(name = "object_key", nullable = false)
	private String objectKey;

	@Column(name = "original_file_name", nullable = false, length = 255)
	private String originalFileName;

	@Column(name = "mime_type", length = 120)
	private String mimeType;

	@Column(name = "size_bytes")
	private Long sizeBytes;

	@Column(name = "checksum_sha256", length = 64)
	private String checksumSha256;

	@Column(name = "etag", length = 120)
	private String etag;

	@Column(name = "uploaded_by_user_id")
	private UUID uploadedByUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	public UUID getId() {
		return id;
	}

	public String getBucketName() {
		return bucketName;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Long getSizeBytes() {
		return sizeBytes;
	}

	public String getChecksumSha256() {
		return checksumSha256;
	}

	public String getEtag() {
		return etag;
	}

	public UUID getUploadedByUserId() {
		return uploadedByUserId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
