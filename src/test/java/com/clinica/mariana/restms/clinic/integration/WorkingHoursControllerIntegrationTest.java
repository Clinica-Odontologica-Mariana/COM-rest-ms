package com.clinica.mariana.restms.clinic.integration;

import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.repository.WorkingHoursRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("WorkingHours integration")
class WorkingHoursControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String WORKING_HOURS_ENDPOINT = "/api/v1/working-hours";
	private static final String WORKING_HOURS_BY_ID_ENDPOINT = "/api/v1/working-hours/{id}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";

	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private WorkingHoursRepository workingHoursRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		workingHoursRepository.deleteAll();
		seedReferenceData();
	}

	private void seedReferenceData() {
		jdbcTemplate.update("merge into clinic (id, name, phone, timezone, active) key(id) values (?, ?, ?, ?, ?)",
				CLINIC_ID, "Clínica Seed", "61999999999", "America/Sao_Paulo", true);
	}

	@Nested
	@DisplayName("Given valid working hours")
	class ValidWorkingHours {

		@Test
		@DisplayName("When created, found by id, updated, listed by clinic and deleted, then the lifecycle is persisted")
		void shouldRunWorkingHoursLifecycle() throws Exception {
			WorkingHoursDto created = createWorkingHours("""
					{
					  "clinicId": "%s",
					  "dayOfWeek": 1,
					  "startTime": "08:00:00",
					  "endTime": "18:00:00"
					}
					""".formatted(CLINIC_ID.toString()));

			assertThat(created.id()).isNotNull();
			assertThat(created.clinicId()).isEqualTo(CLINIC_ID);
			assertThat(created.dayOfWeek()).isEqualTo(1);

			mockMvc.perform(get(WORKING_HOURS_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.dayOfWeek", is(1)))
					.andExpect(jsonPath("$.data.startTime", is("08:00:00")));

			mockMvc.perform(put(WORKING_HOURS_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "dayOfWeek": 2,
							  "startTime": "09:00:00",
							  "endTime": "17:00:00"
							}
							""")).andExpect(status().isOk()).andExpect(jsonPath("$.data.dayOfWeek", is(2)))
					.andExpect(jsonPath("$.data.startTime", is("09:00:00")));

			mockMvc.perform(get(WORKING_HOURS_ENDPOINT).param("clinicId", CLINIC_ID.toString())
					.contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete(WORKING_HOURS_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk());

			mockMvc.perform(get(WORKING_HOURS_ENDPOINT).param("clinicId", CLINIC_ID.toString())
					.contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(0)));
		}
	}

	@Nested
	@DisplayName("Given existing working hours")
	class ExistingWorkingHours {

		@Test
		@DisplayName("When registering the same day of week for a clinic, then the command is rejected")
		void shouldRejectDuplicateDayOfWeekForClinic() throws Exception {
			createWorkingHours("""
					{
					  "clinicId": "%s",
					  "dayOfWeek": 3,
					  "startTime": "08:00:00",
					  "endTime": "18:00:00"
					}
					""".formatted(CLINIC_ID.toString()));

			mockMvc.perform(post(WORKING_HOURS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "clinicId": "%s",
							  "dayOfWeek": 3,
							  "startTime": "09:00:00",
							  "endTime": "12:00:00"
							}
							""".formatted(CLINIC_ID.toString()))).andExpect(status().isConflict());
		}
	}

	private WorkingHoursDto createWorkingHours(String payload) throws Exception {
		String response = mockMvc
				.perform(post(WORKING_HOURS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, WorkingHoursDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
