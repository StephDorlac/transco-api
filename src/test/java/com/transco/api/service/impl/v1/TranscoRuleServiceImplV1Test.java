package com.transco.api.service.impl.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.entity.TranscoRule;
import com.transco.api.exception.ResourceAlreadyExistsException;
import com.transco.api.exception.ResourceNotFoundException;
import com.transco.api.mapper.v1.TranscoRuleMapperV1;
import com.transco.api.repository.TranscoRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranscoRuleServiceImplV1 — Tests unitaires")
class TranscoRuleServiceImplV1Test {

    @Mock
    private TranscoRuleRepository repository;

    @Mock
    private TranscoRuleMapperV1 mapper;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private TranscoRuleServiceImplV1 service;

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private TranscoRuleDtoV1.TranscoRuleRequest buildRequest() {
        return new TranscoRuleDtoV1.TranscoRuleRequest(
                "pays_vers_devise",
                Map.of("pays", "FR"),
                "EUR",
                10
        );
    }

    private TranscoRule buildEntity(Long id) {
        return TranscoRule.builder()
                .id(id)
                .context("pays_vers_devise")
                .inputs(Map.of("pays", "FR"))
                .outputValue("EUR")
                .priority(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TranscoRuleDtoV1.TranscoRuleResponse buildResponse(Long id) {
        return new TranscoRuleDtoV1.TranscoRuleResponse(
                id, "pays_vers_devise", Map.of("pays", "FR"), "EUR", 10,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // -------------------------------------------------------------------------
    // create()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Doit créer et retourner la règle quand elle n'existe pas")
        void shouldCreateRule() {
            var request = buildRequest();
            var entity  = buildEntity(1L);
            var response = buildResponse(1L);

            when(repository.existsByContextAndInputs(any(), any())).thenReturn(false);
            when(mapper.toEntity(request)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            var result = service.create(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.outputValue()).isEqualTo("EUR");
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Doit lever ResourceAlreadyExistsException si la règle existe déjà")
        void shouldThrowWhenRuleAlreadyExists() {
            var request = buildRequest();
            when(repository.existsByContextAndInputs(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("pays_vers_devise");

            verify(repository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Doit retourner la règle si elle existe")
        void shouldReturnRule() {
            var entity   = buildEntity(1L);
            var response = buildResponse(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(entity));
            when(mapper.toResponse(entity)).thenReturn(response);

            var result = service.getById(1L);

            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'id est inconnu")
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // getAll()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Doit retourner toutes les règles")
        void shouldReturnAllRules() {
            var entities  = List.of(buildEntity(1L), buildEntity(2L));
            var responses = List.of(buildResponse(1L), buildResponse(2L));

            when(repository.findAll()).thenReturn(entities);
            when(mapper.toResponseList(entities)).thenReturn(responses);

            var result = service.getAll();

            assertThat(result).hasSize(2);
        }
    }

    // -------------------------------------------------------------------------
    // getByContext()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getByContext()")
    class GetByContext {

        @Test
        @DisplayName("Doit retourner les règles du contexte donné")
        void shouldReturnRulesByContext() {
            var entities  = List.of(buildEntity(1L));
            var responses = List.of(buildResponse(1L));

            when(repository.findByContext("pays_vers_devise")).thenReturn(entities);
            when(mapper.toResponseList(entities)).thenReturn(responses);

            var result = service.getByContext("pays_vers_devise");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).context()).isEqualTo("pays_vers_devise");
        }
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Doit mettre à jour la règle existante")
        void shouldUpdateRule() {
            var request  = buildRequest();
            var entity   = buildEntity(1L);
            var response = buildResponse(1L);

            when(repository.findById(1L)).thenReturn(Optional.of(entity));
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toResponse(entity)).thenReturn(response);

            var result = service.update(1L, request);

            assertThat(result).isNotNull();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'id est inconnu")
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99L, buildRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Doit supprimer la règle existante")
        void shouldDeleteRule() {
            var entity = buildEntity(1L);
            when(repository.findById(1L)).thenReturn(Optional.of(entity));

            service.delete(1L);

            verify(repository).delete(entity);
        }

        @Test
        @DisplayName("Doit lever ResourceNotFoundException si l'id est inconnu")
        void shouldThrowWhenNotFound() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // -------------------------------------------------------------------------
    // resolve()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("resolve()")
    class Resolve {

        @Test
        @DisplayName("Doit retourner resolved=true avec outputValue quand une règle matche (withFallback=true)")
        void shouldResolveWithFallback() {
            var entity  = buildEntity(1L);
            var request = new TranscoRuleDtoV1.TranscoResolveRequest(
                    "pays_vers_devise", Map.of("pays", "FR"), true
            );

            when(repository.resolveWithFallback(eq("pays_vers_devise"), anyString()))
                    .thenReturn(Optional.of(entity));

            var result = service.resolve(request);

            assertThat(result.resolved()).isTrue();
            assertThat(result.outputValue()).isEqualTo("EUR");
            assertThat(result.matchedPriority()).isEqualTo(10);
        }

        @Test
        @DisplayName("Doit retourner resolved=true avec correspondance exacte (withFallback=false)")
        void shouldResolveExact() {
            var entity  = buildEntity(1L);
            var request = new TranscoRuleDtoV1.TranscoResolveRequest(
                    "pays_vers_devise", Map.of("pays", "FR"), false
            );

            when(repository.resolveExact(eq("pays_vers_devise"), anyString()))
                    .thenReturn(Optional.of(entity));

            var result = service.resolve(request);

            assertThat(result.resolved()).isTrue();
            assertThat(result.outputValue()).isEqualTo("EUR");
        }

        @Test
        @DisplayName("Doit retourner resolved=false quand aucune règle ne matche")
        void shouldReturnUnresolvedWhenNoMatch() {
            var request = new TranscoRuleDtoV1.TranscoResolveRequest(
                    "pays_vers_devise", Map.of("pays", "ZZ"), true
            );

            when(repository.resolveWithFallback(eq("pays_vers_devise"), anyString()))
                    .thenReturn(Optional.empty());

            var result = service.resolve(request);

            assertThat(result.resolved()).isFalse();
            assertThat(result.outputValue()).isNull();
        }
    }
}
