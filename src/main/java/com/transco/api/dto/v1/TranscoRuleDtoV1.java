package com.transco.api.dto.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

public class TranscoRuleDtoV1 {

    @Schema(description = "Requête de création ou mise à jour d'une règle de transcodification")
    public record TranscoRuleRequest(

            @Schema(description = "Contexte de la règle", example = "pays_vers_devise")
            @NotBlank(message = "Le contexte est obligatoire")
            String context,

            @Schema(description = "Entrées de la règle sous forme clé/valeur", example = "{\"pays\": \"FR\", \"type\": \"standard\"}")
            @NotNull(message = "Les inputs sont obligatoires")
            Map<String, String> inputs,

            @Schema(description = "Valeur de sortie", example = "EUR")
            @NotBlank(message = "La valeur de sortie est obligatoire")
            String outputValue,

            @Schema(description = "Priorité de la règle (plus élevée = prioritaire)", example = "10")
            Integer priority
    ) {
        public TranscoRuleRequest {
            if (priority == null) priority = 0;
        }
    }

    @Schema(description = "Réponse représentant une règle de transcodification")
    public record TranscoRuleResponse(

            @Schema(description = "Identifiant unique")
            Long id,

            @Schema(description = "Contexte de la règle")
            String context,

            @Schema(description = "Entrées de la règle")
            Map<String, String> inputs,

            @Schema(description = "Valeur de sortie")
            String outputValue,

            @Schema(description = "Priorité")
            Integer priority,

            @Schema(description = "Date de création")
            LocalDateTime createdAt,

            @Schema(description = "Date de dernière modification")
            LocalDateTime updatedAt
    ) {}

    @Schema(description = "Requête de résolution : recherche de la valeur de sortie selon les inputs")
    public record TranscoResolveRequest(

            @Schema(description = "Contexte dans lequel chercher", example = "pays_vers_devise")
            @NotBlank(message = "Le contexte est obligatoire")
            String context,

            @Schema(description = "Valeurs d'entrée à résoudre", example = "{\"pays\": \"FR\"}")
            @NotNull(message = "Les inputs sont obligatoires")
            Map<String, String> inputs,

            @Schema(description = "Si true, utilise le fallback (sous-ensemble). Si false, correspondance exacte.", example = "true")
            boolean withFallback
    ) {}

    @Schema(description = "Résultat de la résolution")
    public record TranscoResolveResponse(

            @Schema(description = "Contexte utilisé")
            String context,

            @Schema(description = "Inputs fournis")
            Map<String, String> inputs,

            @Schema(description = "Valeur de sortie résolue")
            String outputValue,

            @Schema(description = "Priorité de la règle matchée")
            Integer matchedPriority,

            @Schema(description = "Indique si une règle a été trouvée")
            boolean resolved
    ) {}
}
