package com.clinica.mariana.restms.financial.integration;

import com.clinica.mariana.restms.financial.dto.FinancialTransactionDto;
import com.clinica.mariana.restms.financial.repository.FinancialTransactionRepository;
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
@DisplayName("Financial Transaction integration")
class FinancialTransactionControllerIntegrationTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String TRANSACTIONS_ENDPOINT = "/api/v1/financial-transactions";
	private static final String TRANSACTION_BY_ID_ENDPOINT = "/api/v1/financial-transactions/{id}";
	private static final String BY_CLINIC_ENDPOINT = "/api/v1/financial-transactions/by-clinic/{clinicId}";
	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_DOCTOR = "DOCTOR";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private FinancialTransactionRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private UUID defaultClinicId;

	@BeforeEach
	void setupDatabase() {
		repository.deleteAll();
		defaultClinicId = UUID.randomUUID();

		jdbcTemplate.execute("delete from clinic");
		jdbcTemplate.update(
				"insert into clinic (id, name, phone, timezone, active, created_at, updated_at, working_hours_json) "
						+ "values (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)",
				defaultClinicId, "Clínica Teste Financeiro", "11999999999", "America/Sao_Paulo", true, "[]");
	}

	@Nested
	@DisplayName("Given a valid financial transaction")
	class ValidTransaction {

		@Test
		@DisplayName("When created, found by id, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunTransactionLifecycle() throws Exception {
			FinancialTransactionDto created = createTransaction("""
					{
					  "clinicId": "%s",
					  "description": "Limpeza Dental",
					  "type": "RECEITA",
					  "category": "PROCEDIMENTO",
					  "amount": 150.00,
					  "status": "PENDING",
					  "transactionDate": "2026-06-15",
					  "notes": "Paciente deve pagar depois"
					}
					""".formatted(defaultClinicId));

			assertThat(created.id()).isNotNull();
			assertThat(created.status()).isEqualTo("PENDING");

			mockMvc.perform(get(TRANSACTION_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.description", is("Limpeza Dental")));

			mockMvc.perform(put(TRANSACTION_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN)).contentType(MediaType.APPLICATION_JSON).content("""
							{
							  "description": "Limpeza Dental e Aplicação de Flúor",
							  "type": "RECEITA",
							  "category": "PROCEDIMENTO",
							  "amount": 200.00,
							  "status": "PAID",
							  "transactionDate": "2026-06-15",
							  "notes": "Pago no cartão"
							}
							""")).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.description", is("Limpeza Dental e Aplicação de Flúor")))
					.andExpect(jsonPath("$.data.amount", is(200.0))).andExpect(jsonPath("$.data.status", is("PAID")));

			mockMvc.perform(
					get(BY_CLINIC_ENDPOINT, defaultClinicId).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_DOCTOR)))
					.andExpect(status().isOk()).andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete(TRANSACTION_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isNoContent());

			mockMvc.perform(get(TRANSACTION_BY_ID_ENDPOINT, created.id()).contextPath(CONTEXT_PATH)
					.with(jwtWithRole(ROLE_ADMIN))).andExpect(status().isOk())
					.andExpect(jsonPath("$.data.status", is("CANCELLED")));
		}
	}

	private FinancialTransactionDto createTransaction(String payload) throws Exception {
		String response = mockMvc
				.perform(post(TRANSACTIONS_ENDPOINT).contextPath(CONTEXT_PATH).with(jwtWithRole(ROLE_ADMIN))
						.contentType(MediaType.APPLICATION_JSON).content(payload))
				.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

		JsonNode data = objectMapper.readTree(response).get("data");
		return objectMapper.treeToValue(data, FinancialTransactionDto.class);
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
