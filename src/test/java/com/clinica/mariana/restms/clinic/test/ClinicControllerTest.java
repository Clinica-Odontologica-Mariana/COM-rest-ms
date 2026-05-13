package com.clinica.mariana.restms.clinic.test;

import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Clinic integration")
class ClinicControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private ClinicRepository clinicRepository;

	@BeforeEach
	void cleanDatabase() {
		clinicRepository.deleteAll();
	}

	@Nested
	@DisplayName("Given a valid clinic")
	class ValidClinic {

		@Test
		@DisplayName("When created, found by id, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunClinicLifecycle() throws Exception {
			ClinicDto created = createClinic("""
					{
					  "name": "Clinica Mariana",
					  "document": "12345678000199",
					  "phone": "6133334444",
					  "email": "contato@clinicamariana.com",
					  "timezone": "America/Sao_Paulo"
					}
					""");

			assertThat(created.id()).isNotNull();
			assertThat(created.active()).isTrue();
			assertThat(created.timezone()).isEqualTo("America/Sao_Paulo");

			mockMvc.perform(get("/api/v1/clinics/{id}", created.id()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id", is(created.id().toString())))
					.andExpect(jsonPath("$.document", is("12345678000199")));

			mockMvc.perform(put("/api/v1/clinics/{id}", created.id())
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Clinica Mariana Atualizada",
									  "document": "12345678000199",
									  "phone": "61999998888",
									  "email": "novo@clinicamariana.com",
									  "timezone": "America/Sao_Paulo"
									}
									"""))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name", is("Clinica Mariana Atualizada")))
					.andExpect(jsonPath("$.phone", is("61999998888")));

			mockMvc.perform(get("/api/v1/clinics"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(1)));

			mockMvc.perform(delete("/api/v1/clinics/{id}", created.id()))
					.andExpect(status().isNoContent());

			mockMvc.perform(get("/api/v1/clinics/{id}", created.id()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.active", is(false)));

			mockMvc.perform(get("/api/v1/clinics"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(0)));
		}
	}

	@Nested
	@DisplayName("Given invalid clinic commands")
	class InvalidClinicCommands {

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCreatePayloads")
		@DisplayName("When creating, then validation rejects the command")
		void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
			mockMvc.perform(post("/api/v1/clinics")
							.contentType(MediaType.APPLICATION_JSON)
							.content(payload))
					.andExpect(status().isBadRequest());
		}

		static Stream<Arguments> invalidCreatePayloads() {
			return Stream.of(
					Arguments.of("missing name", """
							{
							  "document": "12345678000199",
							  "phone": "6133334444"
							}
							"""),
					Arguments.of("invalid document", """
							{
							  "name": "Clinica Mariana",
							  "document": "123",
							  "phone": "6133334444"
							}
							"""),
					Arguments.of("invalid email", """
							{
							  "name": "Clinica Mariana",
							  "document": "12345678000199",
							  "phone": "6133334444",
							  "email": "email-invalido"
							}
							""")
			);
		}
	}

	@Nested
	@DisplayName("Given an existing clinic")
	class ExistingClinic {

		@Test
		@DisplayName("When another clinic uses the same document, then the command is rejected")
		void shouldRejectDuplicateDocument() throws Exception {
			createClinic("""
					{
					  "name": "Clinica Original",
					  "document": "11122233000144",
					  "phone": "6133334444"
					}
					""");

			mockMvc.perform(post("/api/v1/clinics")
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "name": "Clinica Duplicada",
									  "document": "11122233000144",
									  "phone": "61999998888"
									}
									"""))
					.andExpect(status().isConflict());
		}
	}

	private ClinicDto createClinic(String payload) throws Exception {
		String response = mockMvc.perform(post("/api/v1/clinics")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();

		return objectMapper.readValue(response, ClinicDto.class);
	}
}
