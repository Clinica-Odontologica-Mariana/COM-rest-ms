package com.clinica.mariana.restms.auth.dto;

public record LoginResponseDto(
		String accessToken,
		Long expiresIn,
		String refreshToken,
		Long refreshExpiresIn,
		String tokenType,
		String scope
) {
}
