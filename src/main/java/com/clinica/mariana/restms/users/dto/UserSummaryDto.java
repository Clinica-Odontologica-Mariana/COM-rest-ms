package com.clinica.mariana.restms.users.dto;

public record UserSummaryDto(String id, String username, String email, boolean enabled, String firstName,
		String lastName, String role) {
}
