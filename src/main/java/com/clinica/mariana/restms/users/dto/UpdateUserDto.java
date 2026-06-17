package com.clinica.mariana.restms.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(@Size(max = 100) String firstName, @Size(max = 100) String lastName,
		@Email @Size(max = 150) String email,

		@Pattern(regexp = "^[A-Z_]+$", message = "role must be uppercase, e.g. ADMIN") String role) {
}
