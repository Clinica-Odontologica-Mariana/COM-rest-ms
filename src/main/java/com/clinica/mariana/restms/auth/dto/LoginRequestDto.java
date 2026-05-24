package com.clinica.mariana.restms.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
		@NotBlank(message = "username is required") @Schema(example = "api-admin", description = "Username de teste no Keycloak") String username,

		@NotBlank(message = "password is required") @Schema(example = "api-admin123", description = "Senha de teste no Keycloak") String password) {
}
