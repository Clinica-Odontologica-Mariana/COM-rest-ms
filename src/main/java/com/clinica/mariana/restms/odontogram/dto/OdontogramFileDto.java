package com.clinica.mariana.restms.odontogram.dto;

import com.clinica.mariana.restms.storedfile.dto.StoredFileDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OdontogramFileDto(UUID id, UUID patientId, UUID medicalRecordId, UUID odontogramEntryId,
		StoredFileDto file, String description, OffsetDateTime createdAt) {
}
