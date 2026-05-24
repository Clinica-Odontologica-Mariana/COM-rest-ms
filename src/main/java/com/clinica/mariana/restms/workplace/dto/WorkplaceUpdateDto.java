package com.clinica.mariana.restms.workplace.dto;

import jakarta.validation.constraints.NotBlank;

public record WorkplaceUpdateDto(@NotBlank String name, String description) {
}
