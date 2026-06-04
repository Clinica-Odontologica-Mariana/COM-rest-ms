package com.clinica.mariana.restms.storedfile.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

@Getter
@NoArgsConstructor
@Setter
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

}
