package com.clinica.mariana.restms.professional.integration;

import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
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
@DisplayName("Professional integration")
class ProfessionalControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");
	private static final UUID OTHER_CLINIC_ID = UUID.fromString("0b3f4b57-18de-49a4-b4cb-9e6f72863974");
	private static final UUID SPECIALTY_ID = UUID.fromString("d55c9f29-228d-4f0f-9b74-c3d30eef6f96");
	private static final UUID VALID_USER_ID = UUID.fromString("9f437888-386b-4843-8f16-967ea92410a4");
	private static final UUID OTHER_USER_ID = UUID.fromString("f24db06d-b6fc-43ba-a384-c4bc4da66c40");
	private static final UUID THIRD_USER_ID = UUID.fromString("7e1ef9be-ba45-4ded-b7f6-31635229b7a8");
	private static final UUID FOURTH_USER_ID = UUID.fromString("6f3faa54-8a22-4e2e-b7dc-bde4bda64b70");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private ProfessionalRepository professionalRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		professionalRepository.deleteAll();
		ensureReferenceTables();
		seedReferenceData();
	}

	@Nested
	@DisplayName("Given a valid professional")
	class ValidProfessional {

		@Test
		@DisplayName("When created, found by id, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunProfessionalLifecycle() throws Exception {
			ProfessionalDto created = createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-12345"
					}
					""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID));

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.licenseNumber()).isEqualTo("CRO-DF-12345");

			mockMvc.perform(get("/api/v1/professionals/{id}", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("DOCTOR"))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.licenseNumber", is("CRO-DF-12345")));

			mockMvc.perform(put("/api/v1/professionals/{id}", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("ADMIN")).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-54321"
							}
							""".formatted(OTHER_USER_ID, CLINIC_ID, SPECIALTY_ID))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.userId", is(OTHER_USER_ID.toString())))
					.andExpect(jsonPath("$.data.licenseNumber", is("CRO-DF-54321")));

			mockMvc.perform(get("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(1)));

			mockMvc.perform(delete("/api/v1/professionals/{id}", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("ADMIN"))).andExpect(status().isNoContent());

			mockMvc.perform(get("/api/v1/professionals/{id}", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("DOCTOR"))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.active", is(false)));

			mockMvc.perform(get("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.content", hasSize(0)));

			mockMvc.perform(delete("/api/v1/professionals/{id}", created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("ADMIN"))).andExpect(status().isNoContent());
		}
	}

	@Nested
	@DisplayName("Given an existing professional")
	class ExistingProfessional {

		@Test
		@DisplayName("When another professional uses the same license in the same clinic, then the command is rejected")
		void shouldRejectDuplicateLicensePerClinic() throws Exception {
			createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-99999"
					}
					""".formatted(THIRD_USER_ID, CLINIC_ID, SPECIALTY_ID));

			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-99999"
							}
							""".formatted(FOURTH_USER_ID, CLINIC_ID, SPECIALTY_ID))).andExpect(status().isConflict());
		}

		@Test
		@DisplayName("When another professional uses the same user, then the command is rejected")
		void shouldRejectDuplicateUser() throws Exception {
			createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-10001"
					}
					""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID));

			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-10002"
							}
							""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID))).andExpect(status().isConflict());
		}

		@Test
		@DisplayName("When another professional uses the same license in another clinic, then the command is accepted")
		void shouldAllowSameLicenseInDifferentClinics() throws Exception {
			createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-20001"
					}
					""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID));

			ProfessionalDto createdInOtherClinic = createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-20001"
					}
					""".formatted(OTHER_USER_ID, OTHER_CLINIC_ID, SPECIALTY_ID));

			assertThat(createdInOtherClinic.clinicId()).isEqualTo(OTHER_CLINIC_ID);
			assertThat(createdInOtherClinic.licenseNumber()).isEqualTo("CRO-DF-20001");
		}

		@Test
		@DisplayName("When updating to a license used in the same clinic, then the command is rejected")
		void shouldRejectDuplicateLicenseOnUpdate() throws Exception {
			createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-30001"
					}
					""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID));
			ProfessionalDto second = createProfessional("""
					{
					  "userId": "%s",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-30002"
					}
					""".formatted(OTHER_USER_ID, CLINIC_ID, SPECIALTY_ID));

			mockMvc.perform(put("/api/v1/professionals/{id}", second.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole("ADMIN")).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-30001"
							}
							""".formatted(OTHER_USER_ID, CLINIC_ID, SPECIALTY_ID))).andExpect(status().isConflict());
		}
	}

	@Nested
	@DisplayName("Given missing references")
	class MissingReferences {

		@Test
		@DisplayName("When user does not exist, then returns 404")
		void shouldRejectMissingUser() throws Exception {
			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "3589102e-afca-4759-a7aa-7ad077e2e4a1",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-40001"
							}
							""".formatted(CLINIC_ID, SPECIALTY_ID))).andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("When clinic does not exist, then returns 404")
		void shouldRejectMissingClinic() throws Exception {
			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "09c0a1a8-46c7-478e-b2d7-224c802889c6",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-40002"
							}
							""".formatted(VALID_USER_ID, SPECIALTY_ID))).andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("When specialty does not exist, then returns 404")
		void shouldRejectMissingSpecialty() throws Exception {
			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "ed73131b-39d3-4309-9839-a2dbe0b36763",
							  "licenseNumber": "CRO-DF-40003"
							}
							""".formatted(VALID_USER_ID, CLINIC_ID))).andExpect(status().isNotFound());
		}
	}

	@Nested
	@DisplayName("Given professional access control")
	class ProfessionalAccessControl {

		@Test
		@DisplayName("When called without token, then returns unauthorized")
		void shouldRejectMissingToken() throws Exception {
			mockMvc.perform(get("/api/v1/professionals").contextPath(CONTEXT_PATH)).andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
		}

		@Test
		@DisplayName("When create is called by DOCTOR, then returns created professional")
		void shouldAllowDoctorCreate() throws Exception {
			mockMvc.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("DOCTOR"))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "userId": "%s",
							  "clinicId": "%s",
							  "specialtyId": "%s",
							  "licenseNumber": "CRO-DF-50001"
							}
							""".formatted(VALID_USER_ID, CLINIC_ID, SPECIALTY_ID))).andExpect(status().isCreated())
					.andExpect(jsonPath("$.data.licenseNumber", is("CRO-DF-50001")));
		}
	}

	private ProfessionalDto createProfessional(String payload) throws Exception {
		String response = mockMvc
				.perform(post("/api/v1/professionals").contextPath(CONTEXT_PATH).with(jwtWithRole("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, ProfessionalDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}

	private void ensureReferenceTables() {
		jdbcTemplate.execute("create table if not exists app_user (id uuid primary key)");
		jdbcTemplate.execute("create table if not exists clinic (id uuid primary key)");
		jdbcTemplate.execute("create table if not exists specialty (id uuid primary key)");
	}

	private void seedReferenceData() {
		insertReference("app_user", VALID_USER_ID);
		insertReference("app_user", OTHER_USER_ID);
		insertReference("app_user", THIRD_USER_ID);
		insertReference("app_user", FOURTH_USER_ID);
		insertClinic(CLINIC_ID, "Clinica Principal");
		insertClinic(OTHER_CLINIC_ID, "Clinica Secundaria");
		insertReference("specialty", SPECIALTY_ID);
	}

	private void insertReference(String tableName, UUID id) {
		jdbcTemplate.update("merge into " + tableName + " (id) key(id) values (?)", id);
	}

	private void insertClinic(UUID id, String name) {
		jdbcTemplate.update("""
				merge into clinic (id, name, phone, timezone, active)
				key(id)
				values (?, ?, ?, ?, ?)
				""", id, name, "61999999999", "America/Sao_Paulo", true);
	}
}
