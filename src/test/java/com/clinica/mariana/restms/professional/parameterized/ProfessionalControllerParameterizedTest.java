package com.clinica.mariana.restms.professional.parameterized;

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
@DisplayName("Professional parameterized")
class ProfessionalControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");
	private static final UUID SPECIALTY_ID = UUID.fromString("d55c9f29-228d-4f0f-9b74-c3d30eef6f96");

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("When creating, then validation rejects the command")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/professionals")
					.contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload))
				.andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		return Stream.of(
				Arguments.of("missing userId", """
						{
						  "clinicId": "%s",
						  "specialtyId": "%s",
						  "licenseNumber": "CRO-DF-22222"
						}
						""".formatted(CLINIC_ID, SPECIALTY_ID)),
				Arguments.of("missing clinicId", """
						{
						  "userId": "1fa11324-3ee1-4da2-9747-a7e13c5cd0e6",
						  "specialtyId": "%s",
						  "licenseNumber": "CRO-DF-22222"
						}
						""".formatted(SPECIALTY_ID)),
				Arguments.of("missing specialtyId", """
						{
						  "userId": "1fa11324-3ee1-4da2-9747-a7e13c5cd0e6",
						  "clinicId": "%s",
						  "licenseNumber": "CRO-DF-22222"
						}
						""".formatted(CLINIC_ID)),
				Arguments.of("blank licenseNumber", """
						{
						  "userId": "1fa11324-3ee1-4da2-9747-a7e13c5cd0e6",
						  "clinicId": "%s",
						  "specialtyId": "%s",
						  "licenseNumber": " "
						}
						""".formatted(CLINIC_ID, SPECIALTY_ID))
		);
	}
}
