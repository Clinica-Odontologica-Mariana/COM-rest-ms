package com.clinica.mariana.restms.professional.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfessionalClinicDto(UUID professionalId, UUID clinicId, boolean primaryClinic, boolean active,
		OffsetDateTime createdAt) {
}
