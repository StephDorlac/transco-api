package com.transco.api.controller.v1;

import com.transco.api.dto.v1.ImportResultDto;
import com.transco.api.service.v1.TranscoImportServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/transco-rules")
@RequiredArgsConstructor
@Tag(name = "Import", description = "Import de règles depuis un fichier Excel")
public class TranscoImportControllerV1 {

    private final TranscoImportServiceV1 importService;

    @Operation(summary = "Importer des règles depuis un fichier .xlsx",
               description = "Colonnes fixes : context, output_value, priority. Toutes les autres colonnes sont des critères d'entrée (inputs).")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDto> importExcel(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ImportResultDto(0, 0, 0, java.util.List.of("Fichier vide")));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(new ImportResultDto(0, 0, 0, java.util.List.of("Format invalide : seul .xlsx est accepté")));
        }

        ImportResultDto result = importService.importFromExcel(file);
        return ResponseEntity.ok(result);
    }
}
