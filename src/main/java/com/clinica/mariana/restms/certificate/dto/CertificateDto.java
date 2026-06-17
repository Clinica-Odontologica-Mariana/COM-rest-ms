package com.clinica.mariana.restms.certificate.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificateDto(UUID id, UUID professionalId, String title, String certificateType, String content,
		OffsetDateTime issuedAt, UUID storedFileId, boolean active, OffsetDateTime revokedAt, OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {
}
