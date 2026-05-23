package com.clinica.mariana.restms.address.test;

import com.clinica.mariana.restms.address.dto.AddressDto;
import com.clinica.mariana.restms.address.repository.AddressRepository;
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
@DisplayName("Address integration")
class AddressControllerTest {

	private static final String CONTEXT_PATH = "/api/v1";
	private static final String ADDRESSES_PATH = CONTEXT_PATH + "/addresses";
	private static final String ADDRESS_BY_ID_PATH = ADDRESSES_PATH + "/{id}";
	private static final String ADMIN_ROLE = "ADMIN";
	private static final String DOCTOR_ROLE = "DOCTOR";

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Autowired
	private AddressRepository addressRepository;

	@BeforeEach
	void cleanDatabase() {
		addressRepository.deleteAll();
	}

	@Nested
	@DisplayName("Given a valid address")
	class ValidAddress {

		@Test
		@DisplayName("When created, found by id, updated, listed and deleted, then the lifecycle is persisted")
		void shouldRunAddressLifecycle() throws Exception {
			AddressDto created = createAddress("""
					{
					  "street": "Rua das Flores",
					  "number": "123",
					  "complement": "Sala 2",
					  "neighborhood": "Centro",
					  "city": "Brasilia",
					  "state": "DF",
					  "zipCode": "70000000"
					}
					""");

			assertThat(created.id()).isNotNull();
			assertThat(created.state()).isEqualTo("DF");
			assertThat(created.zipCode()).isEqualTo("70000000");

			mockMvc.perform(get(ADDRESS_BY_ID_PATH, created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(DOCTOR_ROLE)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id", is(created.id().toString())))
					.andExpect(jsonPath("$.data.street", is("Rua das Flores")));

			mockMvc.perform(put(ADDRESS_BY_ID_PATH, created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(ADMIN_ROLE))
							.contentType(MediaType.APPLICATION_JSON)
							.content("""
									{
									  "street": "Avenida Principal",
									  "number": "456",
									  "complement": "Conjunto 10",
									  "neighborhood": "Asa Sul",
									  "city": "Brasilia",
									  "state": "DF",
									  "zipCode": "70000001"
									}
									"""))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.street", is("Avenida Principal")))
					.andExpect(jsonPath("$.data.zipCode", is("70000001")));

			mockMvc.perform(get(ADDRESSES_PATH)
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(DOCTOR_ROLE)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data", hasSize(1)));

			mockMvc.perform(delete(ADDRESS_BY_ID_PATH, created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(ADMIN_ROLE)))
					.andExpect(status().isNoContent());

			mockMvc.perform(get(ADDRESS_BY_ID_PATH, created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(DOCTOR_ROLE)))
					.andExpect(status().isNotFound());

			mockMvc.perform(delete(ADDRESS_BY_ID_PATH, created.id())
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(ADMIN_ROLE)))
					.andExpect(status().isNoContent());
		}

		private AddressDto createAddress(String payload) throws Exception {
			String response = mockMvc.perform(post(ADDRESSES_PATH)
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(ADMIN_ROLE))
							.contentType(MediaType.APPLICATION_JSON)
							.content(payload))
					.andExpect(status().isCreated())
					.andReturn()
					.getResponse()
					.getContentAsString();

			JsonNode data = objectMapper.readTree(response).get("data");
			return objectMapper.treeToValue(data, AddressDto.class);
		}
	}

	@Nested
	@DisplayName("Given invalid address commands")
	class InvalidAddressCommands {

		@ParameterizedTest(name = "{0}")
		@MethodSource("invalidCreatePayloads")
		@DisplayName("When creating, then validation rejects the command")
		void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
			mockMvc.perform(post(ADDRESSES_PATH)
							.contextPath(CONTEXT_PATH)
							.with(jwtWithRole(ADMIN_ROLE))
							.contentType(MediaType.APPLICATION_JSON)
							.content(payload))
					.andExpect(status().isBadRequest());
		}

		static Stream<Arguments> invalidCreatePayloads() {
			return Stream.of(
					Arguments.of("missing street", """
							{
							  "city": "Brasilia",
							  "state": "DF",
							  "zipCode": "70000000"
							}
							"""),
					Arguments.of("invalid lowercase state", """
							{
							  "street": "Rua das Flores",
							  "city": "Brasilia",
							  "state": "df",
							  "zipCode": "70000000"
							}
							"""),
					Arguments.of("invalid long state", """
							{
							  "street": "Rua das Flores",
							  "city": "Brasilia",
							  "state": "DFF",
							  "zipCode": "70000000"
							}
							"""),
					Arguments.of("invalid zip code", """
							{
							  "street": "Rua das Flores",
							  "city": "Brasilia",
							  "state": "DF",
							  "zipCode": "70000-000"
							}
							""")
			);
		}
	}

	private RequestPostProcessor jwtWithRole(String role) {
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
