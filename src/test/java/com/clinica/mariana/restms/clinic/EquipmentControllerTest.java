package com.clinica.mariana.restms.clinic;

import com.clinica.mariana.restms.clinic.dto.EquipmentDto;
import com.clinica.mariana.restms.clinic.repository.EquipmentRepository;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:rest_ms_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=TIMEZONE;INIT=CREATE DOMAIN IF NOT EXISTS CITEXT AS VARCHAR"
})
@DisplayName("Equipment integration")
class EquipmentControllerTest {

    private static final String CONTEXT_PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private EquipmentRepository equipmentRepository;

    @BeforeEach
    void cleanDatabase() {
        equipmentRepository.deleteAll();
    }

    @Nested
    @DisplayName("Given a valid equipment")
    class ValidEquipment {

        @Test
        @DisplayName("When created, found by id, updated, inactivated, listed by clinic and deleted, then the lifecycle is persisted")
        void shouldRunEquipmentLifecycle() throws Exception {
            UUID clinicId = UUID.randomUUID();

            EquipmentDto created = createEquipment("""
                    {
                      "clinicId": "%s",
                      "name": "Raio-X Portátil",
                      "description": "Equipamento principal de Raio-X",
                      "location": "Sala 02"
                    }
                    """.formatted(clinicId.toString()));

            assertThat(created.id()).isNotNull();
            assertThat(created.active()).isTrue();
            assertThat(created.name()).isEqualTo("Raio-X Portátil");
            assertThat(created.clinicId()).isEqualTo(clinicId);

            mockMvc.perform(get("/api/v1/equipment/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(created.id().toString())))
                    .andExpect(jsonPath("$.data.name", is("Raio-X Portátil")));

            mockMvc.perform(put("/api/v1/equipment/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Raio-X Fixo",
                                      "description": "Equipamento atualizado",
                                      "location": "Sala 03"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Raio-X Fixo")))
                    .andExpect(jsonPath("$.data.location", is("Sala 03")));

            mockMvc.perform(get("/api/v1/equipment")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            mockMvc.perform(patch("/api/v1/equipment/{id}/inactivate", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/equipment")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            mockMvc.perform(delete("/api/v1/equipment/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Given invalid equipment commands")
    class InvalidEquipmentCommands {

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCreatePayloads")
        @DisplayName("When creating, then validation rejects the command")
        void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
            mockMvc.perform(post("/api/v1/equipment")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidCreatePayloads() {
            return Stream.of(
                    Arguments.of("missing required name", """
                            {
                              "clinicId": "%s",
                              "description": "Sem nome",
                              "location": "Sala 01"
                            }
                            """.formatted(UUID.randomUUID().toString())),
                    Arguments.of("missing required clinicId", """
                            {
                              "name": "Equipamento Sem Clinica",
                              "description": "Descricao",
                              "location": "Sala 01"
                            }
                            """)
            );
        }
    }

    private EquipmentDto createEquipment(String payload) throws Exception {
        String response = mockMvc.perform(post("/api/v1/equipment")
                        .contextPath(CONTEXT_PATH)
                        .with(jwtWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(response).get("data");
        return objectMapper.treeToValue(data, EquipmentDto.class);
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}