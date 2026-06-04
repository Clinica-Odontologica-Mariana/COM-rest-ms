package com.clinica.mariana.restms.clinic.integration;

import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Clinic integration")
class ClinicControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String CLINICS_ENDPOINT = "/api/v1/clinics";
	private static final String CLINIC_BY_ID_ENDPOINT = "/api/v1/clinics/{id}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";
	private static final String ROLE_RECEPTIONIST = "RECEPTIONIST";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private ClinicRepository clinicRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
		clinicRepository.deleteAll();
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
	}

	@Nested
	@DisplayName("Given a valid clinic")
	class ValidClinic {

		@Test
		@DisplayName("When created, found by id and document, updated, inactivated, listed and deleted, then the lifecycle is persisted")
		void shouldRunClinicLifecycle() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana Matriz",
					  "document": "12345678901234",
					  "phone": "11999999999",
					  "email": "matriz@clinic.com",
					  "timezone": "America/Sao_Paulo"
					}
					""");

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.name()).isEqualTo("Clinica Mariana Matriz");

			mockMvc.perform(
					get(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.document", is("12345678901234")));

			mockMvc.perform(get("/api/v1/clinics/document/{document}", "12345678901234").contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_RECEPTIONIST))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())));

			mockMvc.perform(put(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "name": "Clinica Mariana Matriz Atualizada",
							  "document": "12345678901234",
							  "phone": "11888888888",
							  "email": "matriz.atualizada@clinic.com",
							  "timezone": "America/Sao_Paulo"
							}
							""")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.name", is("Clinica Mariana Matriz Atualizada")))
					.andExpect(jsonPath("$.data.phone", is("11888888888")));

			mockMvc.perform(get(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(1)));

			mockMvc.perform(patch("/api/v1/clinics/{id}/inactivate", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk());

			mockMvc.perform(get(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(0)));
		}

		@Nested
		@DisplayName("Given an existing clinic")
		class ExistingClinic {

			@Test
			@DisplayName("When another clinic uses the same document, then the command is rejected")
			void shouldRejectDuplicateDocument() throws Exception {
				createClinic("""
						{
						  "name": "Clinica Original",
						  "document": "11122233344455",
						  "phone": "1155555555",
						  "email": "original@clinic.com"
						}
						""");

				mockMvc.perform(post(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content("""
								{
								  "name": "Clinica Duplicada",
								  "document": "11122233344455",
								  "phone": "11666666666",
								  "email": "duplicado@clinic.com"
								}
								""")).andExpect(status().isConflict());
			}
		}

		private ClinicDto createClinic(String payload) throws Exception {
			String response = mockMvc
					.perform(post(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
							.contentType(MediaType.APPLICATION_JSON).content(payload))
					.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

			JsonNode data = objectMapper.readTree(response).get("data");
			return objectMapper.treeToValue(data, ClinicDto.class);
		}

		private RequestPostProcessor jwtWithRole(String role) {
			return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
		}
	}
}
