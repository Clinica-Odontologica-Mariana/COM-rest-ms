package com.clinica.mariana.restms.professional.test;

import com.clinica.mariana.restms.professional.dto.ProfessionalDto;
import com.clinica.mariana.restms.professional.repository.ProfessionalRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;
import java.util.stream.Stream;

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
class ProfessionalControllerTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");
	private static final UUID SPECIALTY_ID = UUID.fromString("d55c9f29-228d-4f0f-9b74-c3d30eef6f96");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private ProfessionalRepository professionalRepository;

	@BeforeEach
	void cleanDatabase() {
		professionalRepository.deleteAll();
	}

	@Nested
	@DisplayName("Given a valid professional")
	class ValidProfessional {

		@Test
		@DisplayName("When created, found by id, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunProfessionalLifecycle() throws Exception {
			ProfessionalDto created = createProfessional("""
					{
					  "userId": "9f437888-386b-4843-8f16-967ea92410a4",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-12345"
					}
					""".formatted(CLINIC_ID, SPECIALTY_ID));

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.licenseNumber()).isEqualTo("CRO-DF-12345");

			mockMvc.perform(get("/api/v1/professionals/{id}", created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.licenseNumber", is("CRO-DF-12345")));

			mockMvc.perform(put("/api/v1/professionals/{id}", created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("ADMIN"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "userId": "f24db06d-b6fc-43ba-a384-c4bc4da66c40",
									  "clinicId": "%s",
									  "specialtyId": "%s",
									  "licenseNumber": "CRO-DF-54321"
									}
									""".formatted(CLINIC_ID, SPECIALTY_ID)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.userId", is("f24db06d-b6fc-43ba-a384-c4bc4da66c40")))
					.andExpect(jsonPath("$.data.licenseNumber", is("CRO-DF-54321")));

			mockMvc.perform(get("/api/v1/professionals")
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete("/api/v1/professionals/{id}", created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("ADMIN")))
					.andExpect(status().isNoContent());

			mockMvc.perform(get("/api/v1/professionals/{id}", created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.active", is(false)));

			mockMvc.perform(get("/api/v1/professionals")
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("DOCTOR")))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(0)));
		}
	}

	@Nested
	@DisplayName("Given invalid professional commands")
	class InvalidProfessionalCommands {

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCreatePayloads")
		@DisplayName("When creating, then validation rejects the command")
		void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
			mockMvc.perform(post("/api/v1/professionals")
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("ADMIN"))
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

	@Nested
	@DisplayName("Given an existing professional")
	class ExistingProfessional {

		@Test
		@DisplayName("When another professional uses the same license in the same clinic, then the command is rejected")
		void shouldRejectDuplicateLicensePerClinic() throws Exception {
			createProfessional("""
					{
					  "userId": "7e1ef9be-ba45-4ded-b7f6-31635229b7a8",
					  "clinicId": "%s",
					  "specialtyId": "%s",
					  "licenseNumber": "CRO-DF-99999"
					}
					""".formatted(CLINIC_ID, SPECIALTY_ID));

			mockMvc.perform(post("/api/v1/professionals")
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole("ADMIN"))
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "userId": "6f3faa54-8a22-4e2e-b7dc-bde4bda64b70",
									  "clinicId": "%s",
									  "specialtyId": "%s",
									  "licenseNumber": "CRO-DF-99999"
									}
									""".formatted(CLINIC_ID, SPECIALTY_ID)))
					.andExpect(status().isConflict());
		}
	}

	private ProfessionalDto createProfessional(String payload) throws Exception {
		String response = mockMvc.perform(post("/api/v1/professionals")
						.contextPath(CONTEXT_PATH)
						.with(jwtWithRole("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, ProfessionalDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
