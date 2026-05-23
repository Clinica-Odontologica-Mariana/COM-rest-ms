package com.clinica.mariana.restms.workplace.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Workplace endpoints validating controller-level behavior
 * including request validation and security. Uses parametrized tests for invalid
 * payloads to mirror project patterns in Address and Patient tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Workplace integration (validation + security)")
class WorkplaceControllerIntegrationTest {

    private static final String CONTEXT_PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    // no local object mapper needed for these validation tests

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreatePayloads")
    @DisplayName("When creating workplace, invalid payloads return 400 Bad Request")
    void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
        mockMvc.perform(post("/api/v1/workplaces")
                        .contextPath(CONTEXT_PATH)
                        .with(jwtWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> invalidCreatePayloads() {
        return Stream.of(
                Arguments.of("missing name", "{ \"clinicId\": \"00000000-0000-0000-0000-000000000000\" }"),
                Arguments.of("blank name", "{ \"clinicId\": \"00000000-0000-0000-0000-000000000000\", \"name\": \"\" }"),
                Arguments.of("missing clinicId", "{ \"name\": \"Consultório X\" }"),
                Arguments.of("invalid clinicId format", "{ \"clinicId\": \"not-a-uuid\", \"name\": \"Consultório Y\" }")
        );
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
