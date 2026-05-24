package com.clinica.mariana.restms.patient.parameterized;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Patient parameterized")
class PatientControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("When creating, then validation rejects the command")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/patients")
					.contextPath(CONTEXT_PATH)
					.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload))
				.andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		return Stream.of(
				Arguments.of("missing required birth date", """
						{
						  "fullName": "Sem Data",
						  "cpf": "45678912300",
						  "phone": "11666666666",
						  "email": "sem.data@clinic.com"
						}
						"""),
				Arguments.of("future birth date", """
						{
						  "fullName": "Data Futura",
						  "cpf": "45678912301",
						  "phone": "11666666666",
						  "email": "data.futura@clinic.com",
						  "birthDate": "%s"
						}
						""".formatted(LocalDate.now().plusDays(1))),
				Arguments.of("invalid cpf format", """
						{
						  "fullName": "CPF Invalido",
						  "cpf": "123",
						  "phone": "11666666666",
						  "email": "cpf.invalido@clinic.com",
						  "birthDate": "1990-01-10"
						}
						"""),
				Arguments.of("invalid email format", """
						{
						  "fullName": "Email Invalido",
						  "cpf": "45678912302",
						  "phone": "11666666666",
						  "email": "email-invalido",
						  "birthDate": "1990-01-10"
						}
						""")
		);
	}
}
