package com.clinica.mariana.restms.users.service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.clinica.mariana.restms.auth.properties.KeycloakProperties;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;

@Service
public class UserService {

	private final KeycloakProperties keycloakProperties;
	private final RestClient restClient;

	public UserService(KeycloakProperties keycloakProperties) {
		this.keycloakProperties = keycloakProperties;
		this.restClient = RestClient.create(keycloakProperties.baseUrl());
	}

	public CreateUserResponseDto createUser(CreateUserRequestDto request) {
		String adminAccessToken = requestAdminToken();
		String userId = createKeycloakUser(adminAccessToken, request);
		assignRealmRole(adminAccessToken, userId, request.role());
		return new CreateUserResponseDto(userId, request.username(), request.email(), request.role());
	}

	private String requestAdminToken() {
		Map<String, Object> tokenPayload = requestToken(Map.of(
				"grant_type", "password",
				"client_id", keycloakProperties.clientId(),
				"client_secret", keycloakProperties.clientSecret(),
				"username", keycloakProperties.adminUsername(),
				"password", keycloakProperties.adminPassword(),
				"scope", "openid"));

		return requiredString(tokenPayload, "access_token", "KEYCLOAK_TOKEN_ERROR",
				"Failed to retrieve admin token from Keycloak");
	}

	private Map<String, Object> requestToken(Map<String, String> formValues) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formValues.forEach(formData::add);

		try {
			Map<String, Object> response = restClient.post()
					.uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.realm())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(formData)
					.retrieve()
					.body(Map.class);
			return response == null ? Map.of() : response;
		} catch (RestClientResponseException ex) {
			throw new AppException(
					HttpStatus.UNAUTHORIZED,
					"KEYCLOAK_AUTH_FAILED",
					"Invalid credentials or Keycloak authentication error",
					List.of("status=" + ex.getStatusCode().value()));
		}
	}

	private String createKeycloakUser(String adminAccessToken, CreateUserRequestDto request) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("username", request.username());
		payload.put("enabled", true);
		payload.put("email", request.email());
		payload.put("emailVerified", true);
		payload.put("credentials", List.of(Map.of(
				"type", "password",
				"value", request.password(),
				"temporary", false)));
		if (request.firstName() != null && !request.firstName().isBlank()) {
			payload.put("firstName", request.firstName());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			payload.put("lastName", request.lastName());
		}

		ResponseEntity<Void> response;
		try {
			response = restClient.post()
					.uri("/admin/realms/{realm}/users", keycloakProperties.realm())
					.contentType(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(payload)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
				throw new AppException(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "User already exists in Keycloak");
			}
			throw new AppException(
					HttpStatus.BAD_GATEWAY,
					"KEYCLOAK_CREATE_USER_FAILED",
					"Failed to create user in Keycloak",
					List.of("status=" + ex.getStatusCode().value()));
		}

		URI location = response.getHeaders().getLocation();
		if (location == null || location.getPath() == null || location.getPath().isBlank()) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_CREATE_USER_FAILED",
					"Keycloak did not return created user location");
		}

		String[] segments = location.getPath().split("/");
		String userId = segments[segments.length - 1];
		if (userId.isBlank()) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_CREATE_USER_FAILED",
					"Failed to extract created user id from Keycloak response");
		}
		return userId;
	}

	private void assignRealmRole(String adminAccessToken, String userId, String roleName) {
		RoleRepresentation role = fetchRealmRole(adminAccessToken, roleName);

		try {
			restClient.post()
					.uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", keycloakProperties.realm(), userId)
					.contentType(MediaType.APPLICATION_JSON)
					.headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(List.of(Map.of("id", role.id(), "name", role.name())))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			throw new AppException(
					HttpStatus.BAD_GATEWAY,
					"KEYCLOAK_ASSIGN_ROLE_FAILED",
					"Failed to assign role to user in Keycloak",
					List.of("status=" + ex.getStatusCode().value()));
		}
	}

	private RoleRepresentation fetchRealmRole(String adminAccessToken, String roleName) {
		Map<String, Object> rolePayload;
		try {
			rolePayload = restClient.get()
					.uri("/admin/realms/{realm}/roles/{roleName}", keycloakProperties.realm(), roleName)
					.headers(headers -> headers.setBearerAuth(adminAccessToken))
					.retrieve()
					.body(Map.class);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new AppException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND",
						"Role not found in Keycloak: " + roleName);
			}
			throw new AppException(
					HttpStatus.BAD_GATEWAY,
					"KEYCLOAK_ROLE_FETCH_FAILED",
					"Failed to fetch role from Keycloak",
					List.of("status=" + ex.getStatusCode().value()));
		}

		if (rolePayload == null) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ROLE_FETCH_FAILED",
					"Empty role payload from Keycloak");
		}

		String roleId = requiredString(rolePayload, "id", "KEYCLOAK_ROLE_FETCH_FAILED", "Role payload missing id");
		String role = requiredString(rolePayload, "name", "KEYCLOAK_ROLE_FETCH_FAILED", "Role payload missing name");
		return new RoleRepresentation(roleId, role);
	}

	private String requiredString(Map<String, Object> payload, String key, String code, String message) {
		Object value = payload.get(key);
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		throw new AppException(HttpStatus.BAD_GATEWAY, code, message);
	}

	private record RoleRepresentation(String id, String name) {
	}
}
