package com.clinica.mariana.restms.users.service;

import com.clinica.mariana.restms.auth.properties.KeycloakProperties;
import com.clinica.mariana.restms.common.exception.AppException;
import com.clinica.mariana.restms.users.dto.ChangePasswordDto;
import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;
import com.clinica.mariana.restms.users.dto.UpdateProfileDto;
import com.clinica.mariana.restms.users.dto.UpdateUserDto;
import com.clinica.mariana.restms.users.dto.UserProfileDto;
import com.clinica.mariana.restms.users.dto.UserSummaryDto;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class UserService {

	private static final String PASSWORD = "password";
	private static final String STATUS_PREFIX = "status=";
	private static final String USER_NOT_FOUND_CODE = "USER_NOT_FOUND";
	private static final String USER_NOT_FOUND_MESSAGE = "User not found in Keycloak";
	private static final Set<String> APP_ROLES = Set.of("ADMIN", "RECEPTIONIST", "DOCTOR");

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

	public List<UserSummaryDto> listUsers() {
		String adminAccessToken = requestAdminToken();
		List<Map<String, Object>> payload = fetchUsers(adminAccessToken);
		Map<String, String> roleByUserId = fetchAppRoleByUserId(adminAccessToken);

		return payload.stream().map(user -> toUserSummary(user, roleByUserId)).toList();
	}

	public UserProfileDto getCurrentUser(String userId, String username, Collection<String> roles) {
		String adminAccessToken = requestAdminToken();
		return toProfile(userId, username, fetchUser(adminAccessToken, userId), roles);
	}

	public UserProfileDto updateCurrentUser(String userId, String username, Collection<String> roles,
			UpdateProfileDto request) {
		String adminAccessToken = requestAdminToken();
		Map<String, Object> payload = new LinkedHashMap<>();
		if (request.name() != null && !request.name().isBlank()) {
			String[] parts = request.name().trim().split("\\s+", 2);
			payload.put("firstName", parts[0]);
			payload.put("lastName", parts.length > 1 ? parts[1] : "");
		}
		if (request.email() != null && !request.email().isBlank()) {
			payload.put("email", request.email());
		}
		if (request.phone() != null) {
			payload.put("attributes", Map.of("phone", List.of(request.phone())));
		}
		if (!payload.isEmpty()) {
			updateKeycloakUser(adminAccessToken, userId, payload);
		}
		return toProfile(userId, username, fetchUser(adminAccessToken, userId), roles);
	}

	public void changePassword(String userId, String username, ChangePasswordDto request) {
		ensureNotServiceAccount(username);
		if (!passwordMatches(username, request.currentPassword())) {
			throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD", "Senha atual incorreta");
		}
		resetPassword(requestAdminToken(), userId, request.newPassword());
	}

	public void updateUser(String userId, UpdateUserDto request) {
		String adminAccessToken = requestAdminToken();
		Map<String, Object> payload = new LinkedHashMap<>();
		if (request.firstName() != null) {
			payload.put("firstName", request.firstName());
		}
		if (request.lastName() != null) {
			payload.put("lastName", request.lastName());
		}
		if (request.email() != null && !request.email().isBlank()) {
			payload.put("email", request.email());
		}
		if (!payload.isEmpty()) {
			updateKeycloakUser(adminAccessToken, userId, payload);
		}
		if (request.role() != null && !request.role().isBlank()) {
			replaceRealmRole(adminAccessToken, userId, request.role());
		}
	}

	public void setUserStatus(String userId, boolean enabled) {
		updateKeycloakUser(requestAdminToken(), userId, Map.of("enabled", enabled));
	}

	private void ensureNotServiceAccount(String username) {
		if (username != null && username.equalsIgnoreCase(keycloakProperties.adminUsername())) {
			throw new AppException(HttpStatus.FORBIDDEN, "PROTECTED_SERVICE_ACCOUNT",
					"A conta de serviço do sistema não pode ser alterada nem removida.");
		}
	}

	public void deleteUser(String userId) {
		String adminAccessToken = requestAdminToken();
		ensureNotServiceAccount(readString(fetchUser(adminAccessToken, userId), "username"));
		try {
			restClient.delete().uri("/admin/realms/{realm}/users/{userId}", keycloakProperties.realm(), userId)
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new AppException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE);
			}
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_DELETE_USER_FAILED",
					"Failed to delete user in Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private String requestAdminToken() {
		Map<String, Object> tokenPayload = requestToken(Map.of("grant_type", PASSWORD, "client_id",
				keycloakProperties.clientId(), "client_secret", keycloakProperties.clientSecret(), "username",
				keycloakProperties.adminUsername(), PASSWORD, keycloakProperties.adminPassword(), "scope", "openid"));

		return requiredString(tokenPayload, "access_token", "KEYCLOAK_TOKEN_ERROR",
				"Failed to retrieve admin token from Keycloak");
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
					"Não foi possível autenticar no Keycloak.", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchUsers(String adminAccessToken) {
		try {
			List<?> response = restClient.get().uri("/admin/realms/{realm}/users", keycloakProperties.realm())
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().body(List.class);
			if (response == null) {
				return List.of();
			}
			return (List<Map<String, Object>>) (List<?>) response;
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_LIST_USERS_FAILED",
					"Failed to list users from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private String createKeycloakUser(String adminAccessToken, CreateUserRequestDto request) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("username", request.username());
		payload.put("enabled", true);
		payload.put("email", request.email());
		payload.put("emailVerified", true);
		payload.put("credentials", List.of(Map.of("type", PASSWORD, "value", request.password(), "temporary", false)));
		if (request.firstName() != null && !request.firstName().isBlank()) {
			payload.put("firstName", request.firstName());
		}
		if (request.lastName() != null && !request.lastName().isBlank()) {
			payload.put("lastName", request.lastName());
		}

		ResponseEntity<Void> response;
		try {
			response = restClient.post().uri("/admin/realms/{realm}/users", keycloakProperties.realm())
					.contentType(MediaType.APPLICATION_JSON).headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(payload).retrieve().toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
				throw new AppException(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", "User already exists in Keycloak");
			}
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_CREATE_USER_FAILED",
					"Failed to create user in Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
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
					.contentType(MediaType.APPLICATION_JSON).headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(List.of(Map.of("id", role.id(), "name", role.name()))).retrieve().toBodilessEntity();
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ASSIGN_ROLE_FAILED",
					"Failed to assign role to user in Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private RoleRepresentation fetchRealmRole(String adminAccessToken, String roleName) {
		Map<String, Object> rolePayload;
		try {
			rolePayload = restClient.get()
					.uri("/admin/realms/{realm}/roles/{roleName}", keycloakProperties.realm(), roleName)
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().body(Map.class);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new AppException(HttpStatus.BAD_REQUEST, "ROLE_NOT_FOUND",
						"Role not found in Keycloak: " + roleName);
			}
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ROLE_FETCH_FAILED",
					"Failed to fetch role from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
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

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchUser(String adminAccessToken, String userId) {
		try {
			Map<String, Object> response = restClient.get()
					.uri("/admin/realms/{realm}/users/{userId}", keycloakProperties.realm(), userId)
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().body(Map.class);
			if (response == null) {
				throw new AppException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE);
			}
			return response;
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new AppException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE);
			}
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_FETCH_USER_FAILED",
					"Failed to fetch user from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private void updateKeycloakUser(String adminAccessToken, String userId, Map<String, Object> payload) {
		try {
			restClient.put().uri("/admin/realms/{realm}/users/{userId}", keycloakProperties.realm(), userId)
					.contentType(MediaType.APPLICATION_JSON).headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(payload).retrieve().toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
				throw new AppException(HttpStatus.NOT_FOUND, USER_NOT_FOUND_CODE, USER_NOT_FOUND_MESSAGE);
			}
			if (ex.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
				throw new AppException(HttpStatus.CONFLICT, "USER_CONFLICT", "Email or username already in use");
			}
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_UPDATE_USER_FAILED",
					"Failed to update user in Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private boolean passwordMatches(String username, String password) {
		try {
			Map<String, Object> token = requestToken(Map.of("grant_type", PASSWORD, "client_id",
					keycloakProperties.clientId(), "client_secret", keycloakProperties.clientSecret(), "username",
					username, PASSWORD, password, "scope", "openid"));
			return token.get("access_token") != null;
		} catch (AppException ex) {
			return false;
		}
	}

	private void resetPassword(String adminAccessToken, String userId, String newPassword) {
		try {
			restClient.put()
					.uri("/admin/realms/{realm}/users/{userId}/reset-password", keycloakProperties.realm(), userId)
					.contentType(MediaType.APPLICATION_JSON).headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(Map.of("type", PASSWORD, "value", newPassword, "temporary", false)).retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_RESET_PASSWORD_FAILED",
					"Failed to reset password in Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private void replaceRealmRole(String adminAccessToken, String userId, String newRole) {
		List<Map<String, Object>> current = fetchUserRealmRoles(adminAccessToken, userId);
		List<Map<String, Object>> toRemove = current.stream().filter(role -> {
			Object name = role.get("name");
			return name instanceof String roleName && APP_ROLES.contains(roleName) && !roleName.equals(newRole);
		}).map(role -> Map.<String, Object>of("id", role.get("id"), "name", role.get("name"))).toList();
		if (!toRemove.isEmpty()) {
			removeRealmRoles(adminAccessToken, userId, toRemove);
		}
		assignRealmRole(adminAccessToken, userId, newRole);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchUserRealmRoles(String adminAccessToken, String userId) {
		try {
			List<?> response = restClient.get()
					.uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", keycloakProperties.realm(), userId)
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().body(List.class);
			return response == null ? List.of() : (List<Map<String, Object>>) (List<?>) response;
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ROLE_FETCH_FAILED",
					"Failed to fetch user roles from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private void removeRealmRoles(String adminAccessToken, String userId, List<Map<String, Object>> roles) {
		try {
			restClient.method(HttpMethod.DELETE)
					.uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", keycloakProperties.realm(), userId)
					.contentType(MediaType.APPLICATION_JSON).headers(headers -> headers.setBearerAuth(adminAccessToken))
					.body(roles).retrieve().toBodilessEntity();
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_REMOVE_ROLE_FAILED",
					"Failed to remove roles from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private UserProfileDto toProfile(String userId, String username, Map<String, Object> user,
			Collection<String> roles) {
		String firstName = readString(user, "firstName");
		String lastName = readString(user, "lastName");
		String name = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
		return new UserProfileDto(userId, username, name.isBlank() ? username : name, readString(user, "email"),
				readAttribute(user, "phone"), List.copyOf(roles), readCreatedAt(user));
	}

	private String readAttribute(Map<String, Object> user, String key) {
		Object attributes = user.get("attributes");
		if (attributes instanceof Map<?, ?> attributeMap) {
			Object value = attributeMap.get(key);
			if (value instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof String text) {
				return text;
			}
		}
		return null;
	}

	private String readCreatedAt(Map<String, Object> user) {
		Object createdTimestamp = user.get("createdTimestamp");
		return createdTimestamp instanceof Number number ? Instant.ofEpochMilli(number.longValue()).toString() : null;
	}

	private UserSummaryDto toUserSummary(Map<String, Object> payload, Map<String, String> roleByUserId) {
		String id = readString(payload, "id");
		return new UserSummaryDto(id, readString(payload, "username"), readString(payload, "email"),
				readBoolean(payload, "enabled"), readString(payload, "firstName"), readString(payload, "lastName"),
				id == null ? null : roleByUserId.get(id));
	}

	private Map<String, String> fetchAppRoleByUserId(String adminAccessToken) {
		Map<String, String> roleByUserId = new LinkedHashMap<>();
		for (String role : APP_ROLES) {
			try {
				for (Map<String, Object> user : fetchUsersInRole(adminAccessToken, role)) {
					String userId = readString(user, "id");
					if (userId != null) {
						roleByUserId.put(userId, role);
					}
				}
			} catch (AppException ex) {
				// Role enrichment is best-effort; a failure for one role must not break the
				// listing.
			}
		}
		return roleByUserId;
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> fetchUsersInRole(String adminAccessToken, String roleName) {
		try {
			List<?> response = restClient.get()
					.uri("/admin/realms/{realm}/roles/{roleName}/users", keycloakProperties.realm(), roleName)
					.headers(headers -> headers.setBearerAuth(adminAccessToken)).retrieve().body(List.class);
			return response == null ? List.of() : (List<Map<String, Object>>) (List<?>) response;
		} catch (RestClientResponseException ex) {
			throw new AppException(HttpStatus.BAD_GATEWAY, "KEYCLOAK_ROLE_USERS_FAILED",
					"Failed to fetch users in role from Keycloak", List.of(STATUS_PREFIX + ex.getStatusCode().value()));
		}
	}

	private String readString(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		return value instanceof String text ? text : null;
	}

	private boolean readBoolean(Map<String, Object> payload, String key) {
		Object value = payload.get(key);
		return value instanceof Boolean bool ? bool : false;
	}

	private record RoleRepresentation(String id, String name) {
	}
}
