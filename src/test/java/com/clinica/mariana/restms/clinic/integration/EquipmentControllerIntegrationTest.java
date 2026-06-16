package com.clinica.mariana.restms.clinic.integration;

import com.clinica.mariana.restms.clinic.dto.EquipmentDto;
import com.clinica.mariana.restms.clinic.repository.EquipmentRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Equipment integration")
class EquipmentControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String EQUIPMENT_ENDPOINT = "/api/v1/equipments";
	private static final String EQUIPMENT_BY_ID_ENDPOINT = "/api/v1/equipments/{id}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";

	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private EquipmentRepository equipmentRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		equipmentRepository.deleteAll();
		seedReferenceData();
	}

	private void seedReferenceData() {
		jdbcTemplate.update(
				"merge into clinic (id, name, phone, timezone, working_hours_json, active) key(id) values (?, ?, ?, ?, ?, ?)",
				CLINIC_ID, "Clínica Seed", "61999999999", "America/Sao_Paulo", "[]", true);
	}

	@Nested
	@DisplayName("Given a valid equipment")
	class ValidEquipment {

		@Test
		@DisplayName("When created, found by id, updated, inactivated, listed by clinic and deleted, then the lifecycle is persisted")
		void shouldRunEquipmentLifecycle() throws Exception {
			EquipmentDto created = createEquipment("""
					{
					  "clinicId": "%s",
					  "name": "Raio-X Portátil",
					  "description": "Equipamento principal de Raio-X",
					  "location": "Sala 02"
					}
					""".formatted(CLINIC_ID.toString()));

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.name()).isEqualTo("Raio-X Portátil");
			assertThat(created.clinicId()).isEqualTo(CLINIC_ID);

			mockMvc.perform(
					get(EQUIPMENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.name", is("Raio-X Portátil")));

			mockMvc.perform(put(EQUIPMENT_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "name": "Raio-X Fixo",
							  "description": "Equipamento atualizado",
							  "location": "Sala 03"
							}
							""")).andExpect(status().isOk()).andExpect(jsonPath("$.data.name", is("Raio-X Fixo")))
					.andExpect(jsonPath("$.data.location", is("Sala 03")));

			mockMvc.perform(get(EQUIPMENT_ENDPOINT).param("clinicId", CLINIC_ID.toString()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(patch(EQUIPMENT_BY_ID_ENDPOINT + "/inactivate", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk());

			mockMvc.perform(get(EQUIPMENT_ENDPOINT).param("clinicId", CLINIC_ID.toString()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(0)));
		}
	}

	private EquipmentDto createEquipment(String payload) throws Exception {
		String response = mockMvc
				.perform(post(EQUIPMENT_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, EquipmentDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
