package com.clinica.mariana.restms.medicalrecord.parameterized;

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
@DisplayName("Prontuário parametrizado")
class MedicalRecordControllerParameterizedTest {

	private static final UUID PATIENT_ID = UUID.randomUUID();
	private static final String ALLERGIES = "Penicillin, Shellfish";
	private static final String CHRONIC_CONDITIONS = "Hypertension, Diabetes Type 2";
	private static final String CONTINUOUS_MEDICATIONS = "Metformin 500mg";
	private static final String GENERAL_OBSERVATIONS = "Patient has allergies to penicillin";

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("Ao criar, então a validação rejeita o comando")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/medical-records").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("oversizedFieldPayloads")
	@DisplayName("Ao criar com campos muito grandes, então a validação rejeita o comando")
	void shouldRejectOversizedFields(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/medical-records").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		return Stream.of(Arguments.of("patientId obrigatório ausente", """
				{
				  "allergies": "%s",
				  "chronicConditions": "%s",
				  "continuousMedications": "%s",
				  "generalObservations": "%s"
				}
				""".formatted(ALLERGIES, CHRONIC_CONDITIONS, CONTINUOUS_MEDICATIONS, GENERAL_OBSERVATIONS)));
	}

	static Stream<Arguments> oversizedFieldPayloads() {
		String oversizedString = "x".repeat(4001);
		return Stream.of(Arguments.of("alergias excedem 4000 caracteres", """
				{
				  "patientId": "%s",
				  "allergies": "%s"
				}
				""".formatted(PATIENT_ID, oversizedString)),
				Arguments.of("condiçõesCrônicas excedem 4000 caracteres", """
						{
						  "patientId": "%s",
						  "chronicConditions": "%s"
						}
						""".formatted(PATIENT_ID, oversizedString)),
				Arguments.of("medicamentosContinuos excedem 4000 caracteres", """
						{
						  "patientId": "%s",
						  "continuousMedications": "%s"
						}
						""".formatted(PATIENT_ID, oversizedString)),
				Arguments.of("observaçõesGerais excedem 4000 caracteres", """
						{
						  "patientId": "%s",
						  "generalObservations": "%s"
						}
						""".formatted(PATIENT_ID, oversizedString)));
	}
}
