package com.clinica.mariana.restms.medicalrecord.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MedicalRecordAttachmentDto(
		UUID id,
		UUID medicalRecordId,
		UUID storedFileId,
		String originalFileName,
		String mimeType,
		Long sizeBytes,
		String description,
		OffsetDateTime createdAt
) {
}
