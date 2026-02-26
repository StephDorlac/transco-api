package com.transco.api.controller.v1;

import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.service.v1.TranscoRuleServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transco-rules")
@RequiredArgsConstructor
@Tag(name = "TranscoRule V1", description = "Gestion des règles de transcodification — version 1")
public class TranscoRuleControllerV1 {

    private final TranscoRuleServiceV1 service;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(summary = "Créer une règle", description = "Crée une nouvelle règle de transcodification")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Règle créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Règle déjà existante pour ce contexte et ces inputs")
    })
    public ResponseEntity<TranscoRuleDtoV1.TranscoRuleResponse> create(
            @Valid @RequestBody TranscoRuleDtoV1.TranscoRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une règle par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Règle trouvée"),
            @ApiResponse(responseCode = "404", description = "Règle introuvable")
    })
    public ResponseEntity<TranscoRuleDtoV1.TranscoRuleResponse> getById(
            @Parameter(description = "ID de la règle") @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Lister toutes les règles")
    public ResponseEntity<List<TranscoRuleDtoV1.TranscoRuleResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/by-context")
    @Operation(summary = "Lister les règles d'un contexte")
    public ResponseEntity<List<TranscoRuleDtoV1.TranscoRuleResponse>> getByContext(
            @Parameter(description = "Nom du contexte", example = "pays_vers_devise")
            @RequestParam String context) {
        return ResponseEntity.ok(service.getByContext(context));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une règle")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Règle mise à jour"),
            @ApiResponse(responseCode = "404", description = "Règle introuvable")
    })
    public ResponseEntity<TranscoRuleDtoV1.TranscoRuleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TranscoRuleDtoV1.TranscoRuleRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une règle")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Règle supprimée"),
            @ApiResponse(responseCode = "404", description = "Règle introuvable")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Résolution
    // -------------------------------------------------------------------------

    @PostMapping("/resolve")
    @Operation(
            summary = "Résoudre une transcodification",
            description = """
                    Recherche la valeur de sortie correspondant aux inputs fournis.
                    Avec `withFallback=true`, utilise une correspondance par sous-ensemble (opérateur @>)
                    et retourne la règle la plus prioritaire.
                    Avec `withFallback=false`, la correspondance doit être exacte.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résolution effectuée (resolved=true si trouvée, false sinon)")
    })
    public ResponseEntity<TranscoRuleDtoV1.TranscoResolveResponse> resolve(
            @Valid @RequestBody TranscoRuleDtoV1.TranscoResolveRequest request) {
        return ResponseEntity.ok(service.resolve(request));
    }
}
