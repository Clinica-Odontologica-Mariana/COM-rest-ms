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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private ClinicRepository clinicRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		ensureReferenceTables();
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
		clinicRepository.deleteAll();
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
	}

	@Nested
	@DisplayName("Given a valid clinic")
	class ValidClinic {

		@Test
		@DisplayName("When created, found by id, updated, inactivated and listed, then the lifecycle is persisted")
		void shouldRunClinicLifecycle() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana Matriz",
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
					.andExpect(jsonPath("$.data.name", is("Clinica Mariana Matriz")));

			mockMvc.perform(put(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "name": "Clinica Mariana Matriz Atualizada",
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
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(1)))
					.andExpect(jsonPath("$.data.content[0].active", is(false)));
		}

		@Test
		@DisplayName("When deleted, then clinic is removed from the list and cannot be fetched anymore")
		void shouldDeleteClinicPermanently() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana Removivel",
					  "phone": "11999999999",
					  "email": "remover@clinic.com",
					  "timezone": "America/Sao_Paulo"
					}
					""");

			mockMvc.perform(
					delete(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isNoContent());

			mockMvc.perform(get(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(0)));

			mockMvc.perform(
					get(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("When deleted with embedded working hours, then clinic and schedule data are removed")
		void shouldDeleteClinicWithWorkingHours() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana Agenda",
					  "phone": "11999999999",
					  "email": "agenda@clinic.com",
					  "timezone": "America/Sao_Paulo",
					  "workingHours": [
					    {
					      "dayOfWeek": 0,
					      "startTime": "08:00:00",
					      "endTime": "12:00:00"
					    }
					  ]
					}
					""");
			assertThat(created.workingHours()).hasSize(1);
			assertThat(created.workingHours().get(0).dayOfWeek()).isEqualTo(0);

			mockMvc.perform(
					delete(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isNoContent());

			mockMvc.perform(get(CLINICS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(0)));
		}

		@Test
		@DisplayName("When deleted with linked professionals, then returns a specific conflict message")
		void shouldRejectDeleteClinicWithLinkedProfessionals() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana Profissional",
					  "phone": "11999999999",
					  "email": "profissional@clinic.com",
					  "timezone": "America/Sao_Paulo"
					}
					""");
			UUID userId = UUID.randomUUID();
			UUID specialtyId = UUID.randomUUID();
			UUID professionalId = UUID.randomUUID();

			jdbcTemplate.update(
					"insert into app_user (id, keycloak_subject, full_name, email, email_verified, active) values (?, ?, ?, ?, ?, ?)",
					userId, userId.toString(), "Profissional Teste", userId + "@clinic.com", true, true);
			jdbcTemplate.update("insert into specialty (id, code, name) values (?, ?, ?)", specialtyId,
					"CRO-TESTE-" + specialtyId.toString().substring(0, 8), "Ortodontia Teste");
			jdbcTemplate.update("""
					insert into professional (id, user_id, clinic_id, specialty_id, license_number, active)
					values (?, ?, ?, ?, ?, ?)
					""", professionalId, userId, created.id(), specialtyId, "CRO-BLOQUEIO-001", true);

			mockMvc.perform(
					delete(CLINIC_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isConflict()).andExpect(jsonPath("$.error.message",
							is("Não foi possível excluir a clínica porque há profissionais vinculados a ela.")));
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

	private void ensureReferenceTables() {
		jdbcTemplate.execute("create table if not exists app_user (id uuid primary key)");
		jdbcTemplate.execute("alter table app_user add column if not exists keycloak_subject varchar(100)");
		jdbcTemplate.execute("alter table app_user add column if not exists full_name varchar(150)");
		jdbcTemplate.execute("alter table app_user add column if not exists email varchar(150)");
		jdbcTemplate.execute("alter table app_user add column if not exists email_verified boolean default false");
		jdbcTemplate.execute("alter table app_user add column if not exists active boolean default true");
		jdbcTemplate.execute("""
				create table if not exists specialty (
					id uuid primary key,
					code varchar(50),
					name varchar(100)
				)
				""");
		jdbcTemplate.execute("""
				create table if not exists professional (
					id uuid primary key,
					user_id uuid not null,
					clinic_id uuid not null,
					specialty_id uuid not null,
					license_number varchar(50),
					active boolean default true
				)
				""");
	}
}
