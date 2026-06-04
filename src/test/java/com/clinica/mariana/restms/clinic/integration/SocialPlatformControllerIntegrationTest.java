package com.clinica.mariana.restms.clinic.integration;

import com.clinica.mariana.restms.clinic.entity.SocialPlatformEntity;
import com.clinica.mariana.restms.clinic.repository.SocialPlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("SocialPlatform integration")
class SocialPlatformControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String BASE = "/api/v1/social-platforms";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";
	private static final String ROLE_RECEPTIONIST = "RECEPTIONIST";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SocialPlatformRepository socialPlatformRepository;

	private SocialPlatformEntity savedPlatform;

	@BeforeEach
	void setupDatabase() {
		socialPlatformRepository.deleteAll();

		SocialPlatformEntity entity = new SocialPlatformEntity();
		entity.setCode("INSTAGRAM");
		entity.setName("Instagram");
		savedPlatform = socialPlatformRepository.save(entity);
	}

	@Nested
	@DisplayName("Given existing social platforms")
	class ExistingSocialPlatforms {

		@Test
		@DisplayName("When listing all platforms, then returns the list successfully")
		void shouldListAllPlatforms() throws Exception {
			mockMvc.perform(get(BASE).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(1)))
					.andExpect(jsonPath("$.data[0].code", is("INSTAGRAM")));
		}

		@Test
		@DisplayName("When finding by id, then returns the correct platform")
		void shouldFindById() throws Exception {
			mockMvc.perform(get(BASE + "/{id}", savedPlatform.getId()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_RECEPTIONIST))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(savedPlatform.getId().toString())))
					.andExpect(jsonPath("$.data.name", is("Instagram")));
		}

		@Test
		@DisplayName("When finding by code, then returns the correct platform")
		void shouldFindByCode() throws Exception {
			mockMvc.perform(get(BASE + "/code/{code}", savedPlatform.getCode()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_DOCTOR))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(savedPlatform.getId().toString())))
					.andExpect(jsonPath("$.data.code", is("INSTAGRAM")));
		}
	}

	@Nested
	@DisplayName("Given non-existent social platforms")
	class NonExistentSocialPlatforms {

		@Test
		@DisplayName("When finding by an invalid id, then returns 404 Not Found")
		void shouldReturnNotFoundForInvalidId() throws Exception {
			mockMvc.perform(
					get(BASE + "/{id}", UUID.randomUUID()).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("When finding by an invalid code, then returns 404 Not Found")
		void shouldReturnNotFoundForInvalidCode() throws Exception {
			mockMvc.perform(
					get(BASE + "/code/{code}", "INVALID_CODE").contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN)))
					.andExpect(status().isNotFound());
		}
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
