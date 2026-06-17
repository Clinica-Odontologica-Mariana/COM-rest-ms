package com.clinica.mariana.restms.financial.parameterized;

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

import java.util.UUID;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Financial Transaction parameterized")
class FinancialTransactionControllerParameterizedTest {

	private static final String CONTEXT_PATH = "/api/v1";

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest(name = "{0}")
	@MethodSource("invalidCreatePayloads")
	@DisplayName("When creating, then validation rejects the command")
	void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
		mockMvc.perform(post("/api/v1/financial-transactions").contextPath(CONTEXT_PATH)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
	}

	static Stream<Arguments> invalidCreatePayloads() {
		String validClinicId = UUID.randomUUID().toString();
		return Stream.of(Arguments.of("missing clinicId", """
				{
				  "description": "Limpeza",
				  "type": "RECEITA",
				  "category": "PROCEDIMENTO",
				  "amount": 100.00,
				  "transactionDate": "2026-06-15"
				}
				"""), Arguments.of("missing amount", """
				{
				  "clinicId": "%s",
				  "description": "Limpeza",
				  "type": "RECEITA",
				  "category": "PROCEDIMENTO",
				  "transactionDate": "2026-06-15"
				}
				""".formatted(validClinicId)), Arguments.of("missing transactionDate", """
				{
				  "clinicId": "%s",
				  "description": "Limpeza",
				  "type": "RECEITA",
				  "category": "PROCEDIMENTO",
				  "amount": 100.00
				}
				""".formatted(validClinicId)));
	}
}
