package com.clinica.mariana.restms.appointment.test;

import com.clinica.mariana.restms.appointment.service.GoogleCalendarService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Appointment API")
class AppointmentSecurityTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String BASE = CONTEXT_PATH + "/appointments";
	private static final UUID RANDOM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GoogleCalendarService googleCalendarService;

	@Nested
	@DisplayName("Given an unauthenticated request")
	class UnauthenticatedRequest {

		@ParameterizedTest(name = "{0}")
		@MethodSource("protectedEndpoints")
		@DisplayName("When accessing any endpoint, then 401 is returned")
		void shouldReturn401(String label, MockHttpServletRequestBuilder request) throws Exception {
			mockMvc.perform(request).andExpect(status().isUnauthorized());
		}

		static Stream<Arguments> protectedEndpoints() {
			UUID id = RANDOM_ID;
			return Stream.of(Arguments.of("GET /appointments", get(BASE).contextPath(CONTEXT_PATH)),
					Arguments.of("GET /appointments/period",
							get(BASE + "/period").contextPath(CONTEXT_PATH).param("start", "2026-05-22T09:00:00")
									.param("end", "2026-05-22T18:00:00")),
					Arguments.of("POST /appointments",
							post(BASE).contextPath(CONTEXT_PATH).contentType(MediaType.APPLICATION_JSON).content("{}")),
					Arguments.of("PUT /appointments/{id}",
							put(BASE + "/" + id).contextPath(CONTEXT_PATH).contentType(MediaType.APPLICATION_JSON)
									.content("{}")),
					Arguments.of("DELETE /appointments/{id}", delete(BASE + "/" + id).contextPath(CONTEXT_PATH)));
		}
	}

	@Nested
	@DisplayName("Given an authenticated request")
	class AuthenticatedRequest {

		@Test
		@DisplayName("When listing appointments, then 200 is returned")
		void shouldReturn200OnGetAll() throws Exception {
			mockMvc.perform(get(BASE).contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DOCTOR"))))
					.andExpect(status().isOk());
		}

		@Nested
		@DisplayName("Given invalid create commands")
		class InvalidCreateCommands {

			@ParameterizedTest(name = "{0}")
			@MethodSource("invalidCreatePayloads")
			@DisplayName("When creating, then validation rejects the command")
			void shouldReturn400(String scenario, String payload) throws Exception {
				mockMvc.perform(post(BASE).contextPath(CONTEXT_PATH)
						.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECEPTIONIST")))
						.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
			}

			static Stream<Arguments> invalidCreatePayloads() {
				String validBase = """
						{
						  "patientId": "aaaaaaaa-0000-0000-0000-000000000001",
						  "clinicId": "bbbbbbbb-0000-0000-0000-000000000001",
						  "professionalId": "cccccccc-0000-0000-0000-000000000001",
						  "statusId": "dddddddd-0000-0000-0000-000000000001",
						  "startDatetime": "2026-06-01T09:00:00",
						  "endDatetime": "2026-06-01T10:00:00",
						  "blocksSchedule": false
						}
						""";
				return Stream.of(
						Arguments.of("missing patientId",
								validBase.replace("\"patientId\": \"aaaaaaaa-0000-0000-0000-000000000001\",", "")),
						Arguments.of("missing clinicId",
								validBase.replace("\"clinicId\": \"bbbbbbbb-0000-0000-0000-000000000001\",", "")),
						Arguments.of("missing professionalId",
								validBase.replace("\"professionalId\": \"cccccccc-0000-0000-0000-000000000001\",", "")),
						Arguments.of("missing startDatetime",
								validBase.replace("\"startDatetime\": \"2026-06-01T09:00:00\",", "")),
						Arguments.of("missing endDatetime", validBase
								.replace("\"endDatetime\": \"2026-06-01T10:00:00\"", "\"endDatetime\": null")));
			}
		}
	}
}
