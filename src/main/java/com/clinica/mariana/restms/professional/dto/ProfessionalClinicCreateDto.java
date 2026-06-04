package com.clinica.mariana.restms.professional.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProfessionalClinicCreateDto(@NotNull UUID clinicId, boolean primaryClinic) {
}
