package com.transco.api.security;

import com.transco.api.config.SecurityConfig;
import com.transco.api.controller.v1.TranscoRuleControllerV1;
import com.transco.api.entity.ApiKey;
import com.transco.api.exception.GlobalExceptionHandler;
import com.transco.api.repository.ApiKeyRepository;
import com.transco.api.service.v1.TranscoRuleServiceV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TranscoRuleControllerV1.class)
@Import({SecurityConfig.class, ApiKeyAuthenticationFilter.class, ProblemDetailAuthenticationEntryPoint.class, GlobalExceptionHandler.class})
@DisplayName("ApiKeyAuthenticationFilter — Tests d'intégration sécurité")
class ApiKeyAuthenticationFilterTest {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String BASE_URL = "/api/v1/transco-rules";
    private static final String VALID_KEY = "my-secret-api-key";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private TranscoRuleServiceV1 service;

    // -------------------------------------------------------------------------

    @Test
    @DisplayName("401 — Sans header X-API-Key")
    void shouldReturn401WithoutApiKey() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("https://transco.com/errors/unauthorized"))
                .andExpect(jsonPath("$.title").value("Non autorisé"));
    }

    @Test
    @DisplayName("401 — Avec clé invalide (réponse RFC 7807)")
    void shouldReturn401WithInvalidApiKey() throws Exception {
        when(apiKeyRepository.findByKeyHashAndActiveTrue(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL).header(API_KEY_HEADER, "invalid-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.type").value("https://transco.com/errors/unauthorized"))
                .andExpect(jsonPath("$.title").value("Non autorisé"));
    }

    @Test
    @DisplayName("200 — Avec clé valide")
    void shouldReturn200WithValidApiKey() throws Exception {
        String keyHash = sha256(VALID_KEY);
        ApiKey apiKey = ApiKey.builder()
                .id(1L)
                .clientName("test-client")
                .keyHash(keyHash)
                .active(true)
                .build();

        when(apiKeyRepository.findByKeyHashAndActiveTrue(keyHash)).thenReturn(Optional.of(apiKey));
        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL).header(API_KEY_HEADER, VALID_KEY))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Non-401 — Swagger UI non bloqué par la sécurité")
    void shouldAllowSwaggerWithoutApiKey() throws Exception {
        // La sécurité autorise le chemin. Le slice de test ne sert pas les ressources Swagger,
        // donc le résultat est hors-401 (404 ou 500 selon le handler d'exceptions).
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus(),
                        "Swagger UI ne doit pas être bloqué par la sécurité"));
    }

    // -------------------------------------------------------------------------

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
