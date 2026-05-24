package com.clinica.mariana.restms.patient.integration;

import com.clinica.mariana.restms.patient.dto.PatientDto;
import com.clinica.mariana.restms.patient.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Patient integration")
class PatientControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String PATIENTS_ENDPOINT = "/api/v1/patients";
	private static final String PATIENT_BY_ID_ENDPOINT = "/api/v1/patients/{id}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private PatientRepository patientRepository;

	@BeforeEach
	void cleanDatabase() {
		patientRepository.deleteAll();
	}

	@Nested
	@DisplayName("Given a valid patient")
	class ValidPatient {

		@Test
		@DisplayName("When created, found by id and cpf, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunPatientLifecycle() throws Exception {
			PatientDto created = createPatient("""
					{
					  "fullName": "Maria Silva",
					  "cpf": "12345678901",
					  "phone": "11999999999",
					  "email": "maria.silva@clinic.com",
					  "birthDate": "1990-01-10",
					  "emergencyContactName": "Contato Maria",
					  "emergencyContactPhone": "1133333333",
					  "notes": "Observacao inicial"
					}
					""");

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.emergencyContactName()).isEqualTo("Contato Maria");

			mockMvc.perform(
					get(PATIENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.cpf", is("12345678901")));

			mockMvc.perform(put(PATIENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "fullName": "Maria Silva Atualizada",
							  "cpf": "12345678901",
							  "phone": "11888888888",
							  "email": "maria.atualizada@clinic.com",
							  "birthDate": "1990-01-10",
							  "emergencyContactName": "Contato Atualizado",
							  "emergencyContactPhone": "1144444444",
							  "notes": "Observacao atualizada"
							}
							""")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.fullName", is("Maria Silva Atualizada")))
					.andExpect(jsonPath("$.data.notes", is("Observacao atualizada")));

			mockMvc.perform(get(PATIENTS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete(PATIENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isNoContent());

			mockMvc.perform(
					get(PATIENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.active", is(false)));

			mockMvc.perform(get(PATIENTS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(0)));
		}
	}

	@Nested
	@DisplayName("Given an existing patient")
	class ExistingPatient {

		@Test
		@DisplayName("When another patient uses the same CPF, then the command is rejected")
		void shouldRejectDuplicateCpf() throws Exception {
			createPatient("""
					{
					  "fullName": "Paciente Original",
					  "cpf": "11122233344",
					  "phone": "1155555555",
					  "email": "original@clinic.com",
					  "birthDate": "1991-04-05"
					}
					""");

			mockMvc.perform(post(PATIENTS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "fullName": "Paciente Duplicado",
							  "cpf": "11122233344",
							  "phone": "11666666666",
							  "email": "duplicado@clinic.com",
							  "birthDate": "1991-04-05"
							}
							""")).andExpect(status().isConflict());
		}
	}

	private PatientDto createPatient(String payload) throws Exception {
		String response = mockMvc
				.perform(post(PATIENTS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, PatientDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
