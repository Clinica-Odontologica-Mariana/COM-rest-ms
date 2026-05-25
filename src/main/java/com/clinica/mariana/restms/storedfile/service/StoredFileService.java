package com.clinica.mariana.restms.storedfile.service;

import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.storedfile.dto.PresignedUrlDto;
import com.clinica.mariana.restms.storedfile.dto.StoredFileDto;
import com.clinica.mariana.restms.storedfile.entity.StoredFileEntity;
import com.clinica.mariana.restms.storedfile.model.FileCategory;
import com.clinica.mariana.restms.storedfile.repository.StoredFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class StoredFileService {

	private final StoredFileRepository storedFileRepository;
	private final MinioStorageService minioStorageService;
	private final FileValidationService fileValidationService;
	private final FileNameSanitizer fileNameSanitizer;

	public StoredFileService(StoredFileRepository storedFileRepository, MinioStorageService minioStorageService,
			FileValidationService fileValidationService, FileNameSanitizer fileNameSanitizer) {
		this.storedFileRepository = storedFileRepository;
		this.minioStorageService = minioStorageService;
		this.fileValidationService = fileValidationService;
		this.fileNameSanitizer = fileNameSanitizer;
	}

	@Transactional
	public StoredFileEntity upload(MultipartFile file, FileCategory category, UUID ownerId, UUID uploadedByUserId,
			String description) {
		fileValidationService.validate(category, file);
		byte[] content = readBytes(file);
		String sanitizedFileName = fileNameSanitizer.sanitize(file.getOriginalFilename());
		String objectKey = buildObjectKey(category, ownerId, sanitizedFileName);
		MinioStorageService.UploadResult uploadResult = minioStorageService.upload(content, objectKey,
				file.getContentType());

		try {
			StoredFileEntity entity = new StoredFileEntity();
			entity.setBucketName(uploadResult.bucketName());
			entity.setObjectKey(uploadResult.objectKey());
			entity.setOriginalFileName(sanitizedFileName);
			entity.setMimeType(file.getContentType());
			entity.setSizeBytes(file.getSize());
			entity.setChecksumSha256(sha256(content));
			entity.setEtag(uploadResult.etag());
			entity.setFileCategory(category);
			entity.setUploadedByUserId(uploadedByUserId);
			entity.setDescription(description);
			return storedFileRepository.save(entity);
		} catch (RuntimeException ex) {
			try {
				minioStorageService.remove(objectKey);
			} catch (RuntimeException ignored) {
				// Metadata persistence failed; upload cleanup was attempted and the original
				// failure is preserved.
			}
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public StoredFileEntity findActiveByIdAndCategory(UUID id, FileCategory category) {
		return storedFileRepository.findByIdAndFileCategory(id, category)
				.orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "Stored file not found"));
	}

	@Transactional(readOnly = true)
	public PresignedUrlDto presignedDownloadUrl(UUID id, FileCategory category) {
		StoredFileEntity file = findActiveByIdAndCategory(id, category);
		MinioStorageService.PresignedObjectUrl url = minioStorageService.presignedDownloadUrl(file.getObjectKey());
		return new PresignedUrlDto(url.url(), url.expiresAt());
	}

	@Transactional
	public void hardDelete(StoredFileEntity file) {
		storedFileRepository.delete(file);
		minioStorageService.remove(file.getObjectKey());
	}

	public StoredFileDto toDto(StoredFileEntity entity) {
		return new StoredFileDto(entity.getId(), entity.getOriginalFileName(), entity.getMimeType(),
				entity.getSizeBytes(), entity.getChecksumSha256(), entity.getFileCategory(),
				entity.getUploadedByUserId(), entity.getDescription(), entity.getCreatedAt());
	}

	private String buildObjectKey(FileCategory category, UUID ownerId, String sanitizedFileName) {
		return category.objectKeyPrefix() + "/" + ownerId + "/" + UUID.randomUUID() + "-" + sanitizedFileName;
	}

	private byte[] readBytes(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException ex) {
			throw new AppException(HttpStatus.BAD_REQUEST, "FILE_READ_ERROR", "Failed to read uploaded file");
		}
	}

	private String sha256(byte[] content) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(content));
		} catch (NoSuchAlgorithmException ex) {
			throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "CHECKSUM_ERROR", "Failed to calculate checksum");
		}
	}
}
