package com.clinica.mariana.restms.users.parameterized;

import com.clinica.mariana.restms.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User parameterized")
class UserControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidPayloads")
	@DisplayName("When called by ADMIN, then validation rejects the request")
	void shouldRejectInvalidPayload(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/users").contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(userService);
	}

	static Stream<Arguments> invalidPayloads() {
		return Stream.of(Arguments.of("missing username", """
				{
				  "email": "maria.silva@clinic.local",
				  "password": "SenhaForte123",
				  "role": "DOCTOR"
				}
				"""), Arguments.of("invalid email", """
				{
				  "username": "maria.silva",
				  "email": "maria.silva",
				  "password": "SenhaForte123",
				  "role": "DOCTOR"
				}
				"""), Arguments.of("short password", """
				{
				  "username": "maria.silva",
				  "email": "maria.silva@clinic.local",
				  "password": "1234567",
				  "role": "DOCTOR"
				}
				"""), Arguments.of("invalid role pattern", """
				{
				  "username": "maria.silva",
				  "email": "maria.silva@clinic.local",
				  "password": "SenhaForte123",
				  "role": "doctor"
				}
				"""));
	}
}
