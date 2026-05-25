package com.clinica.mariana.restms.storedfile.dto;

import com.clinica.mariana.restms.storedfile.model.FileCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StoredFileDto(UUID id, String fileName, String mimeType, long sizeBytes, String checksumSha256,
		FileCategory category, UUID uploadedByUserId, String description, OffsetDateTime createdAt) {
}
