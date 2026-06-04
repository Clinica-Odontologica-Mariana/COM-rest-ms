package com.clinica.mariana.restms.auth.service;

import com.clinica.mariana.restms.auth.dto.LoginRequestDto;
import com.clinica.mariana.restms.auth.dto.LoginResponseDto;
import com.clinica.mariana.restms.auth.properties.KeycloakProperties;
import com.clinica.mariana.restms.common.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

	private final KeycloakProperties keycloakProperties;
	private final RestClient restClient;

	public AuthService(KeycloakProperties keycloakProperties) {
		this.keycloakProperties = keycloakProperties;
		this.restClient = RestClient.create(keycloakProperties.baseUrl());
	}

	public LoginResponseDto login(LoginRequestDto request) {
		Map<String, Object> tokenPayload = requestToken(Map.of("grant_type", "password", "client_id",
				keycloakProperties.clientId(), "client_secret", keycloakProperties.clientSecret(), "username",
				request.username(), "password", request.password(), "scope", "openid"));

		return new LoginResponseDto(
				requiredString(tokenPayload, "access_token", "KEYCLOAK_TOKEN_ERROR",
						"Token response missing access_token"),
				readLong(tokenPayload, "expires_in"), readString(tokenPayload, "refresh_token"),
				readLong(tokenPayload, "refresh_expires_in"), readString(tokenPayload, "token_type"),
				readString(tokenPayload, "scope"));
	}

	private Map<String, Object> requestToken(Map<String, String> formValues) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formValues.forEach(formData::add);

		try {
			Map<String, Object> response = restClient.post()
					.uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.realm())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(formData).retrieve().body(Map.class);
			return response == null ? Map.of() : response;
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.UNAUTHORIZED, "KEYCLOAK_AUTH_FAILED",
					"Invalid credentials or Keycloak authentication error",
					List.of("status=" + ex.getStatusCode().value()));
		}
	}

	private String requiredString(Map<String, Object> payload, String key, String code, String message) {
		Object value = payload.get(key);
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		throw new AppException(HttpStatus.BAD_GATEWAY, code, message);
	}

	private String readString(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		return value instanceof String text && !text.isBlank() ? text : null;
	}

	private Long readLong(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		if (value instanceof Number number) {
			return number.longValue();
		}
		return null;
	}
}
