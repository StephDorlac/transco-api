package com.transco.api.mapper.v1;

import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.entity.TranscoRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TranscoRuleMapperV1 — Tests unitaires")
class TranscoRuleMapperV1Test {

    private final TranscoRuleMapperV1 mapper = Mappers.getMapper(TranscoRuleMapperV1.class);

    // -------------------------------------------------------------------------
    // toEntity()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toEntity() — Doit mapper correctement le request vers l'entité")
    void shouldMapRequestToEntity() {
        var request = new TranscoRuleDtoV1.TranscoRuleRequest(
                "pays_vers_devise",
                Map.of("pays", "FR"),
                "EUR",
                10
        );

        TranscoRule entity = mapper.toEntity(request);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull();           // ignoré
        assertThat(entity.getCreatedAt()).isNull();    // ignoré
        assertThat(entity.getUpdatedAt()).isNull();    // ignoré
        assertThat(entity.getContext()).isEqualTo("pays_vers_devise");
        assertThat(entity.getInputs()).containsEntry("pays", "FR");
        assertThat(entity.getOutputValue()).isEqualTo("EUR");
        assertThat(entity.getPriority()).isEqualTo(10);
    }

    @Test
    @DisplayName("toEntity() — La priorité doit valoir 0 par défaut si null dans le request")
    void shouldDefaultPriorityToZero() {
        var request = new TranscoRuleDtoV1.TranscoRuleRequest(
                "ctx", Map.of("k", "v"), "OUT", null
        );

        TranscoRule entity = mapper.toEntity(request);

        // Le record initialise priority à 0 si null (compact constructor)
        assertThat(entity.getPriority()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // toResponse()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toResponse() — Doit mapper correctement l'entité vers la réponse")
    void shouldMapEntityToResponse() {
        var now = LocalDateTime.now();
        var entity = TranscoRule.builder()
                .id(42L)
                .context("pays_vers_devise")
                .inputs(Map.of("pays", "FR"))
                .outputValue("EUR")
                .priority(10)
                .createdAt(now)
                .updatedAt(now)
                .build();

        TranscoRuleDtoV1.TranscoRuleResponse response = mapper.toResponse(entity);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.context()).isEqualTo("pays_vers_devise");
        assertThat(response.inputs()).containsEntry("pays", "FR");
        assertThat(response.outputValue()).isEqualTo("EUR");
        assertThat(response.priority()).isEqualTo(10);
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    // -------------------------------------------------------------------------
    // toResponseList()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toResponseList() — Doit mapper une liste d'entités vers une liste de réponses")
    void shouldMapEntityListToResponseList() {
        var now = LocalDateTime.now();
        var entities = List.of(
                TranscoRule.builder().id(1L).context("ctx1").inputs(Map.of("k", "v1")).outputValue("A").priority(0).createdAt(now).updatedAt(now).build(),
                TranscoRule.builder().id(2L).context("ctx2").inputs(Map.of("k", "v2")).outputValue("B").priority(5).createdAt(now).updatedAt(now).build()
        );

        var responses = mapper.toResponseList(entities);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).id()).isEqualTo(1L);
        assertThat(responses.get(1).id()).isEqualTo(2L);
        assertThat(responses.get(0).outputValue()).isEqualTo("A");
        assertThat(responses.get(1).outputValue()).isEqualTo("B");
    }

    @Test
    @DisplayName("toResponseList() — Doit retourner une liste vide si l'entrée est vide")
    void shouldReturnEmptyListWhenEntitiesEmpty() {
        var responses = mapper.toResponseList(List.of());
        assertThat(responses).isEmpty();
    }
}
