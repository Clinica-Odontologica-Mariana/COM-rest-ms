package com.clinica.mariana.restms.users.dto;

public record CreateUserResponseDto(
		String id,
		String username,
		String email,
		String role
) {
}
