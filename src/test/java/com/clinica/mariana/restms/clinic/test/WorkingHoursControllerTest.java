package com.clinica.mariana.restms.clinic.test;

import com.clinica.mariana.restms.clinic.dto.WorkingHoursDto;
import com.clinica.mariana.restms.clinic.repository.WorkingHoursRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;
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
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:rest_ms_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=TIMEZONE;INIT=CREATE DOMAIN IF NOT EXISTS CITEXT AS VARCHAR"
})
@DisplayName("WorkingHours integration")
class WorkingHoursControllerTest {

    private static final String CONTEXT_PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @BeforeEach
    void cleanDatabase() {
        workingHoursRepository.deleteAll();
    }

    @Nested
    @DisplayName("Given valid working hours")
    class ValidWorkingHours {

        @Test
        @DisplayName("When created, found by id, updated, listed by clinic and deleted, then the lifecycle is persisted")
        void shouldRunWorkingHoursLifecycle() throws Exception {
            UUID clinicId = UUID.randomUUID();

            WorkingHoursDto created = createWorkingHours("""
                    {
                      "clinicId": "%s",
                      "dayOfWeek": 1,
                      "startTime": "08:00:00",
                      "endTime": "18:00:00"
                    }
                    """.formatted(clinicId.toString()));

            assertThat(created.id()).isNotNull();
            assertThat(created.clinicId()).isEqualTo(clinicId);
            assertThat(created.dayOfWeek()).isEqualTo(1);

            mockMvc.perform(get("/api/v1/working-hours/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(created.id().toString())))
                    .andExpect(jsonPath("$.data.dayOfWeek", is(1)))
                    .andExpect(jsonPath("$.data.startTime", is("08:00:00")));

            mockMvc.perform(put("/api/v1/working-hours/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "dayOfWeek": 2,
                                      "startTime": "09:00:00",
                                      "endTime": "17:00:00"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.dayOfWeek", is(2)))
                    .andExpect(jsonPath("$.data.startTime", is("09:00:00")));

            mockMvc.perform(get("/api/v1/working-hours")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            mockMvc.perform(delete("/api/v1/working-hours/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/working-hours")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Given invalid working hours commands")
    class InvalidWorkingHoursCommands {

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCreatePayloads")
        @DisplayName("When creating, then validation rejects the command")
        void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
            mockMvc.perform(post("/api/v1/working-hours")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidCreatePayloads() {
            String randomId = UUID.randomUUID().toString();
            return Stream.of(
                    Arguments.of("missing required clinicId", """
                            {
                              "dayOfWeek": 1,
                              "startTime": "08:00:00",
                              "endTime": "18:00:00"
                            }
                            """),
                    Arguments.of("dayOfWeek less than 0", """
                            {
                              "clinicId": "%s",
                              "dayOfWeek": -1,
                              "startTime": "08:00:00",
                              "endTime": "18:00:00"
                            }
                            """.formatted(randomId)),
                    Arguments.of("dayOfWeek greater than 6", """
                            {
                              "clinicId": "%s",
                              "dayOfWeek": 7,
                              "startTime": "08:00:00",
                              "endTime": "18:00:00"
                            }
                            """.formatted(randomId)),
                    Arguments.of("missing startTime", """
                            {
                              "clinicId": "%s",
                              "dayOfWeek": 1,
                              "endTime": "18:00:00"
                            }
                            """.formatted(randomId))
            );
        }
    }

    @Nested
    @DisplayName("Given existing working hours")
    class ExistingWorkingHours {

        @Test
        @DisplayName("When registering the same day of week for a clinic, then the command is rejected")
        void shouldRejectDuplicateDayOfWeekForClinic() throws Exception {
            UUID clinicId = UUID.randomUUID();

            createWorkingHours("""
                    {
                      "clinicId": "%s",
                      "dayOfWeek": 3,
                      "startTime": "08:00:00",
                      "endTime": "18:00:00"
                    }
                    """.formatted(clinicId.toString()));

            // Tenta criar outro horário para a MESMA clínica e no MESMO dia da semana (3)
            mockMvc.perform(post("/api/v1/working-hours")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "clinicId": "%s",
                                      "dayOfWeek": 3,
                                      "startTime": "09:00:00",
                                      "endTime": "12:00:00"
                                    }
                                    """.formatted(clinicId.toString())))
                    .andExpect(status().isConflict());
        }
    }

    private WorkingHoursDto createWorkingHours(String payload) throws Exception {
        String response = mockMvc.perform(post("/api/v1/working-hours")
                        .contextPath(CONTEXT_PATH)
                        .with(jwtWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(response).get("data");
        return objectMapper.treeToValue(data, WorkingHoursDto.class);
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}