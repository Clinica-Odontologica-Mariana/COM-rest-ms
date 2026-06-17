package com.clinica.mariana.restms.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(@Size(max = 200) String name, @Email @Size(max = 150) String email,
		@Size(max = 30) String phone) {
}
