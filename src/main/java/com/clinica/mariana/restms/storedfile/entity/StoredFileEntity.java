package com.clinica.mariana.restms.storedfile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clinica.mariana.restms.storedfile.model.FileCategory;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "file_category", nullable = false, length = 50)
	private FileCategory fileCategory;

	@Column(name = "uploaded_by_user_id")
	private UUID uploadedByUserId;

	@Column(name = "description")
	private String description;

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

	public FileCategory getFileCategory() {
		return fileCategory;
	}

	public UUID getUploadedByUserId() {
		return uploadedByUserId;
	}

	public String getDescription() {
		return description;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setSizeBytes(Long sizeBytes) {
		this.sizeBytes = sizeBytes;
	}

	public void setChecksumSha256(String checksumSha256) {
		this.checksumSha256 = checksumSha256;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public void setFileCategory(FileCategory fileCategory) {
		this.fileCategory = fileCategory;
	}

	public void setUploadedByUserId(UUID uploadedByUserId) {
		this.uploadedByUserId = uploadedByUserId;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
