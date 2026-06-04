package com.clinica.mariana.restms.certificate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CertificateCreateDto(@NotNull UUID patientId, UUID professionalId,

		@NotBlank @Size(max = 150) String title,

		@NotBlank @Size(max = 50) String certificateType,

		String content,

		OffsetDateTime issuedAt,

		UUID storedFileId) {
}
