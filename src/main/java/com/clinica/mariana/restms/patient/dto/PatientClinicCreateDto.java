package com.clinica.mariana.restms.patient.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PatientClinicCreateDto(@NotNull UUID clinicId, boolean primaryClinic) {
}
