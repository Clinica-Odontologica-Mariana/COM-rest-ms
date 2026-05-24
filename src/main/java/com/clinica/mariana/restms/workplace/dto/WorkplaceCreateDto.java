package com.clinica.mariana.restms.workplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkplaceCreateDto(@NotNull UUID clinicId, @NotBlank String name, String description) {
}
