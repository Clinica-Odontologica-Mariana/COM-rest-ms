package com.clinica.mariana.restms.clinic.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Clinic security")
class ClinicSecurityTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String BASE = "/api/v1/clinics";
	private static final UUID RANDOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@Nested
	@DisplayName("Given an unauthenticated request")
	class UnauthenticatedRequest {

		@ParameterizedTest(name = "{0}")
		@MethodSource("protectedEndpoints")
		@DisplayName("When accessing any endpoint, then 401 is returned")
		void shouldReturn401(String label, MockHttpServletRequestBuilder request) throws Exception {
			mockMvc.perform(request.contextPath(CONTEXT_PATH)).andExpect(status().isUnauthorized());
		}

		static Stream<Arguments> protectedEndpoints() {
			UUID id = RANDOM_ID;
			return Stream.of(Arguments.of("GET /clinics", get(BASE)),
					Arguments.of("GET /clinics/{id}", get(BASE + "/" + id)),
					Arguments.of("POST /clinics", post(BASE).contentType(MediaType.APPLICATION_JSON).content("{}")),
					Arguments.of("PUT /clinics/{id}",
							put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("{}")),
					Arguments.of("DELETE /clinics/{id}", delete(BASE + "/" + id)),
					Arguments.of("PATCH /clinics/{id}/inactivate", patch(BASE + "/" + id + "/inactivate")),
					Arguments.of("PATCH /clinics/{id}/activate", patch(BASE + "/" + id + "/activate")));
		}
	}

	@Nested
	@DisplayName("Given a RECEPTIONIST user on restricted endpoints")
	class ReceptionistOnRestrictedEndpoints {

		@ParameterizedTest(name = "{0}")
		@MethodSource("adminDoctorOnlyEndpoints")
		@DisplayName("When accessing ADMIN/DOCTOR-only endpoints, then 403 is returned")
		void shouldReturn403ForReceptionist(String label, MockHttpServletRequestBuilder request) throws Exception {
			mockMvc.perform(request.contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"))))
					.andExpect(status().isForbidden());
		}

		static Stream<Arguments> adminDoctorOnlyEndpoints() {
			UUID id = RANDOM_ID;
			return Stream.of(Arguments.of("DELETE /clinics/{id}", delete(BASE + "/" + id)),
					Arguments.of("PATCH /clinics/{id}/inactivate", patch(BASE + "/" + id + "/inactivate")),
					Arguments.of("PATCH /clinics/{id}/activate", patch(BASE + "/" + id + "/activate")));
		}
	}

	@Nested
	@DisplayName("Given an authenticated user with no roles")
	class NoRoleUser {

		@ParameterizedTest(name = "{0}")
		@MethodSource("protectedEndpoints")
		@DisplayName("When accessing any clinic endpoint, then 403 is returned")
		void shouldReturn403WithNoRole(String label, MockHttpServletRequestBuilder request) throws Exception {
			mockMvc.perform(request.contextPath(CONTEXT_PATH).with(jwt())).andExpect(status().isForbidden());
		}

		static Stream<Arguments> protectedEndpoints() {
			UUID id = RANDOM_ID;
			return Stream.of(Arguments.of("GET /clinics", get(BASE)),
					Arguments.of("GET /clinics/{id}", get(BASE + "/" + id)),
					Arguments.of("DELETE /clinics/{id}", delete(BASE + "/" + id)),
					Arguments.of("PATCH /clinics/{id}/inactivate", patch(BASE + "/" + id + "/inactivate")));
		}
	}
}
