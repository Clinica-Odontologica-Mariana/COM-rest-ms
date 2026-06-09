package com.clinica.mariana.restms.users.integration;

import com.clinica.mariana.restms.users.dto.CreateUserRequestDto;
import com.clinica.mariana.restms.users.dto.CreateUserResponseDto;
import com.clinica.mariana.restms.users.dto.UserSummaryDto;
import com.clinica.mariana.restms.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User integration")
class UserControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Nested
	@DisplayName("Given listing users")
	class ListUsers {

		@Test
		@DisplayName("When called by ADMIN, then returns users list")
		void shouldListUsers() throws Exception {
			doReturn(java.util.List.of(
					new UserSummaryDto("u-1", "api-admin", "api-admin@rest-ms.local", true, "API", "Admin"),
					new UserSummaryDto("u-2", "maria.silva", "maria.silva@clinic.local", true, "Maria", "Silva")))
					.when(userService).listUsers();

			mockMvc.perform(get("/api/v1/users").contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk())
					.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.length()").value(2))
					.andExpect(jsonPath("$.data[0].username").value("api-admin"))
					.andExpect(jsonPath("$.data[1].username").value("maria.silva"));

			verify(userService).listUsers();
		}

		@Test
		@DisplayName("When called without token, then returns unauthorized")
		void shouldReturnUnauthorizedForMissingToken() throws Exception {
			mockMvc.perform(get("/api/v1/users").contextPath(CONTEXT_PATH)).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

			verifyNoInteractions(userService);
		}

		@Test
		@DisplayName("When called with non-admin role, then returns forbidden")
		void shouldReturnForbiddenForNonAdmin() throws Exception {
			mockMvc.perform(get("/api/v1/users").contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
					.andExpect(status().isForbidden()).andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

			verifyNoInteractions(userService);
		}
	}

	@Nested
	@DisplayName("Given a valid create user request")
	class ValidCreateUserRequest {

		@Test
		@DisplayName("When called by ADMIN, then returns created user payload")
		void shouldCreateUser() throws Exception {
			CreateUserRequestDto request = new CreateUserRequestDto("maria.silva", "maria.silva@clinic.local", "Maria",
					"Silva", "SenhaForte123", "DOCTOR");
			CreateUserResponseDto response = new CreateUserResponseDto("09da415e-cebc-44ea-91ff-7512f126642b",
					"maria.silva", "maria.silva@clinic.local", "DOCTOR");
			when(userService.createUser(eq(request))).thenReturn(response);

			mockMvc.perform(post("/api/v1/users").contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "username": "maria.silva",
							  "email": "maria.silva@clinic.local",
							  "firstName": "Maria",
							  "lastName": "Silva",
							  "password": "SenhaForte123",
							  "role": "DOCTOR"
							}
							""")).andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
					.andExpect(jsonPath("$.data.id").value("09da415e-cebc-44ea-91ff-7512f126642b"))
					.andExpect(jsonPath("$.data.username").value("maria.silva"))
					.andExpect(jsonPath("$.data.email").value("maria.silva@clinic.local"))
					.andExpect(jsonPath("$.data.role").value("DOCTOR"));

			verify(userService).createUser(eq(request));
		}
	}

	@Nested
	@DisplayName("Given create user access control")
	class CreateUserAccessControl {

		@Test
		@DisplayName("When called without token, then returns unauthorized")
		void shouldReturnUnauthorizedWhenMissingToken() throws Exception {
			mockMvc.perform(post("/api/v1/users").contextPath(CONTEXT_PATH).contentType(MediaType.APPLICATION_JSON)
					.content("{}")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

			verifyNoInteractions(userService);
		}

		@Test
		@DisplayName("When called with non-admin role, then returns forbidden")
		void shouldReturnForbiddenForNonAdmin() throws Exception {
			mockMvc.perform(post("/api/v1/users").contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "username": "maria.silva",
							  "email": "maria.silva@clinic.local",
							  "firstName": "Maria",
							  "lastName": "Silva",
							  "password": "SenhaForte123",
							  "role": "DOCTOR"
							}
							""")).andExpect(status().isForbidden()).andExpect(jsonPath("$.success").value(false))
					.andExpect(jsonPath("$.error.code").value("FORBIDDEN"));

			verifyNoInteractions(userService);
		}
	}

}
