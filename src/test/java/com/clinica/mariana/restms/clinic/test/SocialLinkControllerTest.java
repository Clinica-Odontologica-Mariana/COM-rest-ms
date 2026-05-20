package com.clinica.mariana.restms.clinic.test;

import com.clinica.mariana.restms.clinic.dto.SocialLinkDto;
import com.clinica.mariana.restms.clinic.repository.SocialLinkRepository;
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
@DisplayName("SocialLink integration")
class SocialLinkControllerTest {

    private static final String CONTEXT_PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private SocialLinkRepository socialLinkRepository;

    @BeforeEach
    void cleanDatabase() {
        socialLinkRepository.deleteAll();
    }

    @Nested
    @DisplayName("Given a valid social link")
    class ValidSocialLink {

        @Test
        @DisplayName("When created, found by id, updated, listed by clinic and deleted, then the lifecycle is persisted")
        void shouldRunSocialLinkLifecycle() throws Exception {
            UUID clinicId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();

            SocialLinkDto created = createSocialLink("""
                    {
                      "clinicId": "%s",
                      "platformId": "%s",
                      "url": "https://instagram.com/clinicamariana"
                    }
                    """.formatted(clinicId.toString(), platformId.toString()));

            assertThat(created.id()).isNotNull();
            assertThat(created.clinicId()).isEqualTo(clinicId);
            assertThat(created.platformId()).isEqualTo(platformId);
            assertThat(created.url()).isEqualTo("https://instagram.com/clinicamariana");

            mockMvc.perform(get("/api/v1/social-links/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(created.id().toString())))
                    .andExpect(jsonPath("$.data.url", is("https://instagram.com/clinicamariana")));

            mockMvc.perform(put("/api/v1/social-links/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "platformId": "%s",
                                      "url": "https://facebook.com/clinicamariana"
                                    }
                                    """.formatted(platformId.toString())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.url", is("https://facebook.com/clinicamariana")));

            mockMvc.perform(get("/api/v1/social-links")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            mockMvc.perform(delete("/api/v1/social-links/{id}", created.id())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/social-links")
                            .param("clinicId", clinicId.toString())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Given invalid social link commands")
    class InvalidSocialLinkCommands {

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidCreatePayloads")
        @DisplayName("When creating, then validation rejects the command")
        void shouldRejectInvalidCreatePayloads(String scenario, String payload) throws Exception {
            mockMvc.perform(post("/api/v1/social-links")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }

        static Stream<Arguments> invalidCreatePayloads() {
            String randomId = UUID.randomUUID().toString();
            return Stream.of(
                    Arguments.of("missing required url", """
                            {
                              "clinicId": "%s",
                              "platformId": "%s"
                            }
                            """.formatted(randomId, randomId)),
                    Arguments.of("missing required platformId", """
                            {
                              "clinicId": "%s",
                              "url": "https://link.com"
                            }
                            """.formatted(randomId)),
                    Arguments.of("invalid url format", """
                            {
                              "clinicId": "%s",
                              "platformId": "%s",
                              "url": "www.instagram.com/clinicamariana"
                            }
                            """.formatted(randomId, randomId)) // Faltou o http:// ou https://
            );
        }
    }

    @Nested
    @DisplayName("Given an existing social link")
    class ExistingSocialLink {

        @Test
        @DisplayName("When another link uses the same platform for the same clinic, then the command is rejected")
        void shouldRejectDuplicatePlatformForClinic() throws Exception {
            UUID clinicId = UUID.randomUUID();
            UUID platformId = UUID.randomUUID();

            createSocialLink("""
                    {
                      "clinicId": "%s",
                      "platformId": "%s",
                      "url": "https://instagram.com/original"
                    }
                    """.formatted(clinicId.toString(), platformId.toString()));

            // Tenta criar outro link para a MESMA clínica e MESMA plataforma
            mockMvc.perform(post("/api/v1/social-links")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "clinicId": "%s",
                                      "platformId": "%s",
                                      "url": "https://instagram.com/duplicado"
                                    }
                                    """.formatted(clinicId.toString(), platformId.toString())))
                    .andExpect(status().isConflict());
        }
    }

    private SocialLinkDto createSocialLink(String payload) throws Exception {
        String response = mockMvc.perform(post("/api/v1/social-links")
                        .contextPath(CONTEXT_PATH)
                        .with(jwtWithRole("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper.readTree(response).get("data");
        return objectMapper.treeToValue(data, SocialLinkDto.class);
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}