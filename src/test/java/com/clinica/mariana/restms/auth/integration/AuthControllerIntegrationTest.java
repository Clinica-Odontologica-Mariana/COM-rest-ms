package com.clinica.mariana.restms.auth.integration;

import com.clinica.mariana.restms.auth.dto.LoginRequestDto;
import com.clinica.mariana.restms.auth.dto.LoginResponseDto;
import com.clinica.mariana.restms.auth.service.AuthService;
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

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth integration")
class AuthControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Nested
	@DisplayName("Given valid login credentials")
	class ValidLogin {

		@Test
		@DisplayName("When login is requested, then token payload is returned")
		void shouldReturnTokenPayload() throws Exception {
			LoginRequestDto request = new LoginRequestDto("api-admin", "api-admin123");
			LoginResponseDto response = new LoginResponseDto("access-token", 86400L, "refresh-token", 1800L, "Bearer",
					"openid profile email");
			when(authService.login(eq(request))).thenReturn(response);

			mockMvc.perform(post("/api/v1/auth/login").contextPath(CONTEXT_PATH).contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "username": "api-admin",
							  "password": "api-admin123"
							}
							""")).andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true)))
					.andExpect(jsonPath("$.data.accessToken", is("access-token")))
					.andExpect(jsonPath("$.data.expiresIn", is(86400)))
					.andExpect(jsonPath("$.data.refreshToken", is("refresh-token")))
					.andExpect(jsonPath("$.data.refreshExpiresIn", is(1800)))
					.andExpect(jsonPath("$.data.tokenType", is("Bearer")));

			verify(authService).login(eq(request));
		}
	}

	@Nested
	@DisplayName("Given the authenticated user endpoint")
	class AuthenticatedUserEndpoint {

		@Test
		@DisplayName("When called with a JWT, then principal claims are returned")
		void shouldReturnAuthenticatedUser() throws Exception {
			mockMvc.perform(get("/api/v1/auth/me").contextPath(CONTEXT_PATH).with(jwt()
					.jwt(jwt -> jwt.subject("subject-123").claim("preferred_username", "api-admin")
							.claim("email", "api-admin@rest-ms.local")
							.claim("realm_access", Map.of("roles", List.of("ADMIN", "DOCTOR"))))
					.authorities(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_DOCTOR"))))
					.andExpect(status().isOk()).andExpect(jsonPath("$.success", is(true)))
					.andExpect(jsonPath("$.data.subject", is("subject-123")))
					.andExpect(jsonPath("$.data.username", is("api-admin")))
					.andExpect(jsonPath("$.data.email", is("api-admin@rest-ms.local")))
					.andExpect(jsonPath("$.data.roles", containsInAnyOrder("ADMIN", "DOCTOR")));
		}

		@Test
		@DisplayName("When called without token, then request is unauthorized")
		void shouldRejectWhenMissingToken() throws Exception {
			mockMvc.perform(get("/api/v1/auth/me").contextPath(CONTEXT_PATH)).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.success", is(false)))
					.andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
		}
	}
}
