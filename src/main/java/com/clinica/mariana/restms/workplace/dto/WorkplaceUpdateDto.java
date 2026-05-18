package com.clinica.mariana.restms.workplace.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record WorkplaceUpdateDto(
        @NotBlank String name,
        String description
) {
}
