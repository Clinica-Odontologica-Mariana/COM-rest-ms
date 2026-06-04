package com.clinica.mariana.restms.odontogram.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OdontogramEntryDto(UUID id, UUID medicalRecordId, UUID patientId, Integer toothNumber, String surfaceCode,
		String conditionCode, String notes, UUID recordedByProfessionalId, OffsetDateTime recordedAt) {
}
