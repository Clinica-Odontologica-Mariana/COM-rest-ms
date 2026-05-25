package com.clinica.mariana.restms.appointment.test;

import com.clinica.mariana.restms.appointment.dto.AppointmentDto;
import com.clinica.mariana.restms.appointment.repository.AppointmentRepository;
import com.clinica.mariana.restms.appointment.service.GoogleCalendarService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Appointment HTTP integration")
class AppointmentControllerHttpTest {

	private static final String BASE = "/api/v1/appointments";
	private static final String DATA_PATH = "$.data";
	private static final String STATUS_SCHEDULED = "aaaaaaaa-0000-0000-0000-000000000001";
	private static final String STATUS_CONFIRMED = "aaaaaaaa-0000-0000-0000-000000000002";
	private static final String SCHEDULED = "SCHEDULED";
	private static final String CONFIRMED = "CONFIRMED";
	private static final String SYNCED = "SYNCED";
	private static final String CONSULTA_DE_ROTINA = "Consulta de rotina";
	private static final String CONSULTA_CONFIRMADA = "Consulta confirmada";
	private static final String GOOGLE_EVENT_HTTP_TEST = "google-event-http-test";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppointmentRepository appointmentRepository;

	@MockitoBean
	private GoogleCalendarService googleCalendarService;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@BeforeEach
	void setup() throws IOException {
		appointmentRepository.deleteAll();
		when(googleCalendarService.createEvent(any(), any(), any(), any())).thenReturn(GOOGLE_EVENT_HTTP_TEST);
	}

	@Nested
	@DisplayName("Given a valid appointment")
	class ValidAppointment {

		@Test
		@DisplayName("When created, then 201 is returned with appointment data")
		void shouldCreateAndReturnAppointment() throws Exception {
			mockMvc.perform(post(BASE).with(jwtAsReceptionist()).contentType(MediaType.APPLICATION_JSON)
					.content(buildCreatePayload(STATUS_SCHEDULED))).andExpect(status().isCreated())
					.andExpect(jsonPath("$.data.id", notNullValue()))
					.andExpect(jsonPath("$.data.statusCode", is(SCHEDULED)))
					.andExpect(jsonPath("$.data.notes", is(CONSULTA_DE_ROTINA)))
					.andExpect(jsonPath("$.data.externalCalendarEventId", is(GOOGLE_EVENT_HTTP_TEST)))
					.andExpect(jsonPath("$.data.calendarSyncStatusCode", is(SYNCED)));
		}

		@Test
		@DisplayName("When listing all, then 200 is returned with non-empty list")
		void shouldListAllAppointments() throws Exception {
			createAppointment(STATUS_SCHEDULED);

			mockMvc.perform(get(BASE).with(jwtAsReceptionist())).andExpect(status().isOk())
					.andExpect(jsonPath(DATA_PATH, hasSize(greaterThanOrEqualTo(1))));
		}

		@Test
		@DisplayName("When listing by period, then 200 is returned with matching appointments")
		void shouldListAppointmentsByPeriod() throws Exception {
			AppointmentDto created = createAppointment(STATUS_SCHEDULED);

			OffsetDateTime start = created.startDatetime().minusHours(1);
			OffsetDateTime end = created.endDatetime().plusHours(1);

			mockMvc.perform(get(BASE + "/period").with(jwtAsReceptionist())
					.param("start", start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
					.param("end", end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))).andExpect(status().isOk())
					.andExpect(jsonPath(DATA_PATH, hasSize(greaterThanOrEqualTo(1))))
					.andExpect(jsonPath("$.data[0].id", is(created.id().toString())));
		}

		@Test
		@DisplayName("When updated, then 200 is returned with new data")
		void shouldUpdateAppointment() throws Exception {
			AppointmentDto created = createAppointment(STATUS_SCHEDULED);

			OffsetDateTime newStart = created.startDatetime().plusMinutes(30);
			OffsetDateTime newEnd = created.endDatetime().plusMinutes(30);

			mockMvc.perform(put(BASE + "/" + created.id()).with(jwtAsReceptionist())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "statusId": "%s",
							  "startDatetime": "%s",
							  "endDatetime": "%s",
							  "notes": "%s",
							  "blocksSchedule": true
							}
							""".formatted(STATUS_CONFIRMED, newStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
							newEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), CONSULTA_CONFIRMADA)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data.statusCode", is(CONFIRMED)))
					.andExpect(jsonPath("$.data.notes", is(CONSULTA_CONFIRMADA)));
		}

		@Test
		@DisplayName("When deleted, then 204 is returned and appointment is no longer listed")
		void shouldDeleteAppointment() throws Exception {
			AppointmentDto created = createAppointment(STATUS_SCHEDULED);

			mockMvc.perform(delete(BASE + "/" + created.id()).with(jwtAsReceptionist()))
					.andExpect(status().isNoContent());

			mockMvc.perform(get(BASE).with(jwtAsReceptionist())).andExpect(status().isOk())
					.andExpect(jsonPath(DATA_PATH, hasSize(0)));
		}

		private AppointmentDto createAppointment(String statusId) throws Exception {
			String response = mockMvc
					.perform(post(BASE).with(jwtAsReceptionist()).contentType(MediaType.APPLICATION_JSON)
							.content(buildCreatePayload(statusId)))
					.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

			JsonNode data = objectMapper.readTree(response).get("data");
			AppointmentDto dto = objectMapper.treeToValue(data, AppointmentDto.class);
			assertThat(dto.id()).isNotNull();
			return dto;
		}

		private String buildCreatePayload(String statusId) {
			OffsetDateTime start = OffsetDateTime.now().plusDays(1).withNano(0);
			OffsetDateTime end = start.plusHours(1);
			return """
					{
					  "patientId": "%s",
					  "clinicId": "%s",
					  "professionalId": "%s",
					  "statusId": "%s",
					  "startDatetime": "%s",
					  "endDatetime": "%s",
					  "notes": "%s",
					  "blocksSchedule": true
					}
					""".formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), statusId,
					start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
					end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), CONSULTA_DE_ROTINA);
		}
	}

	@Nested
	@DisplayName("Given a non-existent appointment")
	class NonExistentAppointment {

		@Test
		@DisplayName("When updating, then 404 is returned")
		void shouldReturn404OnUpdate() throws Exception {
			UUID nonExistentId = UUID.randomUUID();

			mockMvc.perform(put(BASE + "/" + nonExistentId).with(jwtAsReceptionist())
					.contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "statusId": "%s",
							  "startDatetime": "2026-06-01T09:00:00Z",
							  "endDatetime": "2026-06-01T10:00:00Z",
							  "notes": "Teste",
							  "blocksSchedule": false
							}
							""".formatted(STATUS_CONFIRMED))).andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("When deleting, then 404 is returned")
		void shouldReturn404OnDelete() throws Exception {
			UUID nonExistentId = UUID.randomUUID();

			mockMvc.perform(delete(BASE + "/" + nonExistentId).with(jwtAsReceptionist()))
					.andExpect(status().isNotFound());
		}
	}

	private org.springframework.test.web.servlet.request.RequestPostProcessor jwtAsReceptionist() {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"));
	}
}
