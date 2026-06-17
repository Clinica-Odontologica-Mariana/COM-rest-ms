package com.clinica.mariana.restms.users.dto;

import java.util.List;

public record UserProfileDto(String id, String username, String name, String email, String phone, List<String> roles,
		String createdAt) {
}
