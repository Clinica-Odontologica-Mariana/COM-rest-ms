package com.clinica.mariana.restms.auth.parameterized;

import com.clinica.mariana.restms.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth parameterized")
class AuthControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidLoginPayloads")
	@DisplayName("When login is requested, then validation rejects the request")
	void shouldRejectInvalidLoginPayload(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/auth/login").contextPath(CONTEXT_PATH).contentType(MediaType.APPLICATION_JSON)
				.content(payload)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.success", is(false)))
				.andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

		verifyNoInteractions(authService);
	}

	static Stream<Arguments> invalidLoginPayloads() {
		return Stream.of(Arguments.of("missing username", """
				{
				  "password": "api-admin123"
				}
				"""), Arguments.of("missing password", """
				{
				  "username": "api-admin"
				}
				"""), Arguments.of("blank username", """
				{
				  "username": " ",
				  "password": "api-admin123"
				}
				"""), Arguments.of("blank password", """
				{
				  "username": "api-admin",
				  "password": " "
				}
				"""));
	}
}
