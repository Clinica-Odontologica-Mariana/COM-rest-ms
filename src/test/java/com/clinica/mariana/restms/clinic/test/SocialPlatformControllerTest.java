package com.clinica.mariana.restms.clinic.test;

import com.clinica.mariana.restms.clinic.entity.SocialPlatformEntity;
import com.clinica.mariana.restms.clinic.repository.SocialPlatformRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:rest_ms_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=TIMEZONE;INIT=CREATE DOMAIN IF NOT EXISTS CITEXT AS VARCHAR"
})
@DisplayName("SocialPlatform integration")
class SocialPlatformControllerTest {

    private static final String CONTEXT_PATH = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SocialPlatformRepository socialPlatformRepository;

    private SocialPlatformEntity savedPlatform;

    @BeforeEach
    void setupDatabase() {
        socialPlatformRepository.deleteAll();

        SocialPlatformEntity entity = new SocialPlatformEntity();
        entity.setCode("INSTAGRAM");
        entity.setName("Instagram");
        savedPlatform = socialPlatformRepository.save(entity);
    }

    @Nested
    @DisplayName("Given existing social platforms")
    class ExistingSocialPlatforms {

        @Test
        @DisplayName("When listing all platforms, then returns the list successfully")
        void shouldListAllPlatforms() throws Exception {
            mockMvc.perform(get("/api/v1/social-platforms")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].code", is("INSTAGRAM")));
        }

        @Test
        @DisplayName("When finding by id, then returns the correct platform")
        void shouldFindById() throws Exception {
            mockMvc.perform(get("/api/v1/social-platforms/{id}", savedPlatform.getId())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("RECEPTIONIST")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(savedPlatform.getId().toString())))
                    .andExpect(jsonPath("$.data.name", is("Instagram")));
        }

        @Test
        @DisplayName("When finding by code, then returns the correct platform")
        void shouldFindByCode() throws Exception {
            mockMvc.perform(get("/api/v1/social-platforms/code/{code}", savedPlatform.getCode())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("DOCTOR")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(savedPlatform.getId().toString())))
                    .andExpect(jsonPath("$.data.code", is("INSTAGRAM")));
        }
    }

    @Nested
    @DisplayName("Given non-existent social platforms")
    class NonExistentSocialPlatforms {

        @Test
        @DisplayName("When finding by an invalid id, then returns 404 Not Found")
        void shouldReturnNotFoundForInvalidId() throws Exception {
            mockMvc.perform(get("/api/v1/social-platforms/{id}", UUID.randomUUID())
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("When finding by an invalid code, then returns 404 Not Found")
        void shouldReturnNotFoundForInvalidCode() throws Exception {
            mockMvc.perform(get("/api/v1/social-platforms/code/{code}", "INVALID_CODE")
                            .contextPath(CONTEXT_PATH)
                            .with(jwtWithRole("ADMIN")))
                    .andExpect(status().isNotFound());
        }
    }

    private RequestPostProcessor jwtWithRole(String role) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}