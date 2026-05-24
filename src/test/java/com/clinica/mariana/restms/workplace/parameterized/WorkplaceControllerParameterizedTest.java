package com.clinica.mariana.restms.workplace.parameterized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Local de trabalho parametrizado")
class WorkplaceControllerParameterizedTest {

	private static final UUID CLINIC_ID = UUID.randomUUID();

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("Ao criar, então a validação rejeita o comando")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/workplaces")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		return Stream.of(
				Arguments.of("clinicId obrigatório ausente", """
						{
						  "name": "Consultation Room 1",
						  "description": "Main consultation room"
						}
						"""),
				Arguments.of("nome obrigatório ausente", """
						{
						  "clinicId": "%s",
						  "description": "Main consultation room"
						}
						""".formatted(CLINIC_ID)),
				Arguments.of("nome vazio", """
						{
						  "clinicId": "%s",
						  "name": "",
						  "description": "Main consultation room"
						}
						""".formatted(CLINIC_ID)),
				Arguments.of("nome em branco com espaços", """
						{
						  "clinicId": "%s",
						  "name": "   ",
						  "description": "Main consultation room"
						}
						""".formatted(CLINIC_ID)));
	}
}
