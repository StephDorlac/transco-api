package com.transco.api.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.exception.GlobalExceptionHandler;
import com.transco.api.exception.ResourceNotFoundException;
import com.transco.api.service.v1.TranscoRuleServiceV1;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscoRuleControllerV1.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TranscoRuleControllerV1 — Tests unitaires")
class TranscoRuleControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TranscoRuleServiceV1 service;

    private static final String BASE_URL = "/api/v1/transco-rules";

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private TranscoRuleDtoV1.TranscoRuleResponse buildResponse(Long id) {
        return new TranscoRuleDtoV1.TranscoRuleResponse(
                id, "pays_vers_devise", Map.of("pays", "FR"), "EUR", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private TranscoRuleDtoV1.TranscoRuleRequest buildRequest() {
        return new TranscoRuleDtoV1.TranscoRuleRequest(
                "pays_vers_devise", Map.of("pays", "FR"), "EUR", 10
        );
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/transco-rules
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/transco-rules")
    class PostCreate {

        @Test
        @DisplayName("201 — Doit créer une règle et retourner la réponse")
        void shouldReturn201OnCreate() throws Exception {
            var request  = buildRequest();
            var response = buildResponse(1L);

            when(service.create(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.outputValue").value("EUR"))
                    .andExpect(jsonPath("$.context").value("pays_vers_devise"));
        }

        @Test
        @DisplayName("400 — Doit retourner une erreur si le context est vide")
        void shouldReturn400WhenContextBlank() throws Exception {
            var invalid = new TranscoRuleDtoV1.TranscoRuleRequest(
                    "", Map.of("pays", "FR"), "EUR", 10
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.context").exists());
        }

        @Test
        @DisplayName("400 — Doit retourner une erreur si outputValue est vide")
        void shouldReturn400WhenOutputValueBlank() throws Exception {
            var invalid = new TranscoRuleDtoV1.TranscoRuleRequest(
                    "pays_vers_devise", Map.of("pays", "FR"), "", 10
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.outputValue").exists());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/transco-rules/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/transco-rules/{id}")
    class GetById {

        @Test
        @DisplayName("200 — Doit retourner la règle existante")
        void shouldReturn200() throws Exception {
            when(service.getById(1L)).thenReturn(buildResponse(1L));

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.outputValue").value("EUR"));
        }

        @Test
        @DisplayName("404 — Doit retourner 404 si la règle est introuvable")
        void shouldReturn404() throws Exception {
            when(service.getById(99L)).thenThrow(new ResourceNotFoundException("Règle introuvable : id=99"));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Règle introuvable : id=99"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/transco-rules
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/transco-rules")
    class GetAll {

        @Test
        @DisplayName("200 — Doit retourner la liste de toutes les règles")
        void shouldReturnAll() throws Exception {
            when(service.getAll()).thenReturn(List.of(buildResponse(1L), buildResponse(2L)));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/transco-rules/by-context
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/transco-rules/by-context")
    class GetByContext {

        @Test
        @DisplayName("200 — Doit retourner les règles du contexte demandé")
        void shouldReturnByContext() throws Exception {
            when(service.getByContext("pays_vers_devise")).thenReturn(List.of(buildResponse(1L)));

            mockMvc.perform(get(BASE_URL + "/by-context").param("context", "pays_vers_devise"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].context").value("pays_vers_devise"));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/transco-rules/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/transco-rules/{id}")
    class PutUpdate {

        @Test
        @DisplayName("200 — Doit mettre à jour et retourner la règle")
        void shouldReturn200OnUpdate() throws Exception {
            var request  = buildRequest();
            var response = buildResponse(1L);

            when(service.update(eq(1L), any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("404 — Doit retourner 404 si la règle est introuvable")
        void shouldReturn404OnUpdate() throws Exception {
            when(service.update(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Règle introuvable : id=99"));

            mockMvc.perform(put(BASE_URL + "/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest())))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/transco-rules/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/transco-rules/{id}")
    class DeleteById {

        @Test
        @DisplayName("204 — Doit supprimer la règle et retourner 204")
        void shouldReturn204() throws Exception {
            doNothing().when(service).delete(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 — Doit retourner 404 si la règle est introuvable")
        void shouldReturn404() throws Exception {
            doThrow(new ResourceNotFoundException("Règle introuvable : id=99"))
                    .when(service).delete(99L);

            mockMvc.perform(delete(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/transco-rules/resolve
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/transco-rules/resolve")
    class Resolve {

        @Test
        @DisplayName("200 — Doit retourner resolved=true si une règle matche")
        void shouldReturnResolvedTrue() throws Exception {
            var request = new TranscoRuleDtoV1.TranscoResolveRequest(
                    "pays_vers_devise", Map.of("pays", "FR"), true
            );
            var response = new TranscoRuleDtoV1.TranscoResolveResponse(
                    "pays_vers_devise", Map.of("pays", "FR"), "EUR", 10, true
            );

            when(service.resolve(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL + "/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resolved").value(true))
                    .andExpect(jsonPath("$.outputValue").value("EUR"))
                    .andExpect(jsonPath("$.matchedPriority").value(10));
        }

        @Test
        @DisplayName("200 — Doit retourner resolved=false si aucune règle ne matche")
        void shouldReturnResolvedFalse() throws Exception {
            var request = new TranscoRuleDtoV1.TranscoResolveRequest(
                    "pays_vers_devise", Map.of("pays", "ZZ"), true
            );
            var response = new TranscoRuleDtoV1.TranscoResolveResponse(
                    "pays_vers_devise", Map.of("pays", "ZZ"), null, null, false
            );

            when(service.resolve(any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL + "/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resolved").value(false))
                    .andExpect(jsonPath("$.outputValue").doesNotExist());
        }
    }
}
