package com.clinica.mariana.restms.patient.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PatientClinicDto(UUID patientId, UUID clinicId, boolean primaryClinic, boolean active,
		OffsetDateTime createdAt) {
}
