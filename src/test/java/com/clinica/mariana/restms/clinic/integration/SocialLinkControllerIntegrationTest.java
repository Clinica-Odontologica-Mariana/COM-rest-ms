package com.clinica.mariana.restms.clinic.integration;

import com.clinica.mariana.restms.clinic.dto.SocialLinkDto;
import com.clinica.mariana.restms.clinic.repository.SocialLinkRepository;
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
@DisplayName("SocialLink integration")
class SocialLinkControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String SOCIAL_LINKS_ENDPOINT = "/api/v1/social-links";
	private static final String SOCIAL_LINK_BY_ID_ENDPOINT = "/api/v1/social-links/{id}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";

	private static final UUID CLINIC_ID = UUID.fromString("c8ab8aa8-6ce6-49a8-aef7-ee58920f66f8");
	private static final UUID PLATFORM_ID = UUID.fromString("d55c9f29-228d-4f0f-9b74-c3d30eef6f96");

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private SocialLinkRepository socialLinkRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		socialLinkRepository.deleteAll();
		seedReferenceData();
	}

	private void seedReferenceData() {
		jdbcTemplate.update(
				"merge into clinic (id, name, document, phone, timezone, active) key(id) values (?, ?, ?, ?, ?, ?)",
				CLINIC_ID, "Clínica Seed", "12345678000195", "61999999999", "America/Sao_Paulo", true);
		jdbcTemplate.update("merge into social_platform (id, code, name) key(id) values (?, ?, ?)", PLATFORM_ID,
				"INSTAGRAM", "Instagram");
	}

	@Nested
	@DisplayName("Given a valid social link")
	class ValidSocialLink {

		@Test
		@DisplayName("When created, found by id, updated, listed by clinic and deleted, then the lifecycle is persisted")
		void shouldRunSocialLinkLifecycle() throws Exception {
			SocialLinkDto created = createSocialLink("""
					{
					  "clinicId": "%s",
					  "platformId": "%s",
					  "url": "https://instagram.com/clinicamariana"
					}
					""".formatted(CLINIC_ID.toString(), PLATFORM_ID.toString()));

			assertThat(created.id()).isNotNull();
			assertThat(created.clinicId()).isEqualTo(CLINIC_ID);
			assertThat(created.platformId()).isEqualTo(PLATFORM_ID);
			assertThat(created.url()).isEqualTo("https://instagram.com/clinicamariana");

			mockMvc.perform(get(SOCIAL_LINK_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.url", is("https://instagram.com/clinicamariana")));

			mockMvc.perform(put(SOCIAL_LINK_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "platformId": "%s",
							  "url": "https://facebook.com/clinicamariana"
							}
							""".formatted(PLATFORM_ID.toString()))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.url", is("https://facebook.com/clinicamariana")));

			mockMvc.perform(get(SOCIAL_LINKS_ENDPOINT).param("clinicId", CLINIC_ID.toString()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete(SOCIAL_LINK_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk());

			mockMvc.perform(get(SOCIAL_LINKS_ENDPOINT).param("clinicId", CLINIC_ID.toString()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(0)));
		}
	}

	@Nested
	@DisplayName("Given an existing social link")
	class ExistingSocialLink {

		@Test
		@DisplayName("When another link uses the same platform for the same clinic, then the command is rejected")
		void shouldRejectDuplicatePlatformForClinic() throws Exception {
			createSocialLink("""
					{
					  "clinicId": "%s",
					  "platformId": "%s",
					  "url": "https://instagram.com/original"
					}
					""".formatted(CLINIC_ID.toString(), PLATFORM_ID.toString()));

			mockMvc.perform(post(SOCIAL_LINKS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "clinicId": "%s",
							  "platformId": "%s",
							  "url": "https://instagram.com/duplicado"
							}
							""".formatted(CLINIC_ID.toString(), PLATFORM_ID.toString())))
					.andExpect(status().isConflict());
		}
	}

	private SocialLinkDto createSocialLink(String payload) throws Exception {
		String response = mockMvc
				.perform(post(SOCIAL_LINKS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, SocialLinkDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
