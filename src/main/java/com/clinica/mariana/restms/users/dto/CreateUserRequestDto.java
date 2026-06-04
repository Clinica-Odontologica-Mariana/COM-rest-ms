package com.clinica.mariana.restms.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDto(
		@NotBlank(message = "username is required") @Size(max = 100, message = "username must have at most 100 characters") @Schema(example = "maria.silva") String username,

		@NotBlank(message = "email is required") @Email(message = "email must be valid") @Size(max = 150, message = "email must have at most 150 characters") @Schema(example = "maria.silva@clinic.local") String email,

		@Size(max = 100, message = "firstName must have at most 100 characters") @Schema(example = "Maria") String firstName,

		@Size(max = 100, message = "lastName must have at most 100 characters") @Schema(example = "Silva") String lastName,

		@NotBlank(message = "password is required") @Size(min = 8, max = 100, message = "password must have between 8 and 100 characters") @Schema(example = "SenhaForte123") String password,

		@NotBlank(message = "role is required") @Pattern(regexp = "^[A-Z_]+$", message = "role must be uppercase, e.g. ADMIN") @Schema(example = "DOCTOR", allowableValues = {
				"ADMIN", "RECEPTIONIST", "DOCTOR"}) String role){
}
