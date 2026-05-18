package com.clinica.mariana.restms.workplace.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record WorkplaceCreateDto(
        @NotBlank UUID clinicId,
        @NotBlank String name,
        String description
) {
}
