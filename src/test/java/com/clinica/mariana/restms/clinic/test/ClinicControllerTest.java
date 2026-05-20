package com.clinica.mariana.restms.clinic.test;

import com.clinica.mariana.restms.clinic.dto.ClinicDto;
import com.clinica.mariana.restms.clinic.repository.ClinicRepository;

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

import org.springframework.test.context.TestPropertySource;
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
        "spring.datasource.url=jdbc:h2:mem:rest_ms_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=TIMEZONE",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@DisplayName("Clinic integration")
class ClinicControllerTest {

    private static final String CONTEXT_PATH = "/api/v1";

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
        @DisplayName("When created, found by id and document(CNPJ), updated, inactivated, listed and deleted, then the lifecycle is persisted")
        void shouldRunClinicLifecycle() throws Exception {
            ClinicDto created = createClinic("""
                    {
                      "name": "Clinica Mariana Matriz",
                      "document": "12345678901234",
                      "phone": "11999999999",
                      "email": "matriz@clinic.com",
                      "timezone": "America/Sao_Paulo"
                    }
                    """);

            assertThat(created.id()).isNotNull();
            assertThat(created.active()).isTrue();
            assertThat(created.name()).isEqualTo("Clinica Mariana Matriz");

            mockMvc.perform(get("/api/v1/clinics/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(created.id().toString())))
                    .andExpect(jsonPath("$.data.document", is("12345678901234")));

            mockMvc.perform(get("/api/v1/clinics/document/{document}", "12345678901234")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("RECEPTIONIST")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(created.id().toString())));

            mockMvc.perform(put("/api/v1/clinics/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Clinica Mariana Matriz Atualizada",
                                      "document": "12345678901234",
                                      "phone": "11888888888",
                                      "email": "matriz.atualizada@clinic.com",
                                      "timezone": "America/Sao_Paulo"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name", is("Clinica Mariana Matriz Atualizada")))
                    .andExpect(jsonPath("$.data.phone", is("11888888888")));

            mockMvc.perform(get("/api/v1/clinics")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            mockMvc.perform(patch("/api/v1/clinics/{id}/inactivate", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/clinics")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            mockMvc.perform(delete("/api/v1/clinics/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());
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
                              "document": "45678912300000",
                              "phone": "11666666666",
                              "email": "sem.nome@clinic.com"
                            }
                            """),
                    Arguments.of("invalid document format", """
                            {
                              "name": "Clinica Documento Invalido",
                              "document": "123",
                              "phone": "11666666666",
                              "email": "doc.invalido@clinic.com"
                            }
                            """),
                    Arguments.of("invalid email format", """
                            {
                              "name": "Clinica Email Invalido",
                              "document": "45678912300001",
                              "phone": "11666666666",
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
        @DisplayName("When another clinic uses the same document(CNPJ), then the command is rejected")
        void shouldRejectDuplicateDocument() throws Exception {
            createClinic("""
                    {
                      "name": "Clinica Original",
                      "document": "11122233344455",
                      "phone": "1155555555",
                      "email": "original@clinic.com"
                    }
                    """);

            mockMvc.perform(post("/api/v1/clinics")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Clinica Duplicada",
                                      "document": "11122233344455",
                                      "phone": "11666666666",
                                      "email": "duplicado@clinic.com"
                                    }
                                    """))
                    .andExpect(status().isConflict());
        }
    }

    private ClinicDto createClinic(String payload) throws Exception {
        String response = mockMvc.perform(post("/api/v1/clinics")
                        .contextPath(CONTEXT_PATH)
                        .with(jwtWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(response).get("data");
        return objectMapper.treeToValue(data, ClinicDto.class);
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}