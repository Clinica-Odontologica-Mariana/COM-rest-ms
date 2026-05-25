package com.clinica.mariana.restms.clinic.parameterized;

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
@DisplayName("Equipment parameterized")
class EquipmentControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("When creating, then validation rejects the command")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/equipments").contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		return Stream.of(Arguments.of("missing clinicId reference", """
				{
				  "name": "Equipamento Sem Clinica",
				  "description": "Descricao",
				  "location": "Sala 01"
				}
				"""), Arguments.of("missing required name", """
				{
				  "clinicId": "%s",
				  "description": "Sem nome",
				  "location": "Sala 01"
				}
				""".formatted(CLINIC_ID)));
	}
}
