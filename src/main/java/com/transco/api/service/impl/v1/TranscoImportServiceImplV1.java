package com.transco.api.service.impl.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transco.api.dto.v1.ImportResultDto;
import com.transco.api.entity.TranscoRule;
import com.transco.api.repository.TranscoRuleRepository;
import com.transco.api.service.v1.TranscoImportServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscoImportServiceImplV1 implements TranscoImportServiceV1 {

    private static final Set<String> FIXED_COLUMNS = Set.of("context", "output_value", "priority");

    private final TranscoRuleRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public ImportResultDto importFromExcel(MultipartFile file) {
        int inserted = 0, skipped = 0, rejected = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return new ImportResultDto(0, 0, 0, List.of("Fichier vide ou en-tête manquant"));
            }

            // Lecture des en-têtes
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim().toLowerCase());
            }

            if (!headers.contains("context") || !headers.contains("output_value")) {
                return new ImportResultDto(0, 0, 0,
                        List.of("Colonnes obligatoires manquantes : 'context' et 'output_value' sont requises"));
            }

            // Colonnes dynamiques = tout ce qui n'est pas fixe
            List<String> inputColumns = headers.stream()
                    .filter(h -> !FIXED_COLUMNS.contains(h))
                    .toList();

            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, formatter)) continue;

                int rowNum = i + 1;
                try {
                    String context = getCellValue(row, headers.indexOf("context"), formatter);
                    String outputValue = getCellValue(row, headers.indexOf("output_value"), formatter);

                    if (context.isBlank() || outputValue.isBlank()) {
                        errors.add("Ligne %d : 'context' et 'output_value' sont obligatoires".formatted(rowNum));
                        rejected++;
                        continue;
                    }

                    int priority = 0;
                    int priorityIdx = headers.indexOf("priority");
                    if (priorityIdx >= 0) {
                        String pStr = getCellValue(row, priorityIdx, formatter);
                        if (!pStr.isBlank()) {
                            try { priority = Integer.parseInt(pStr); }
                            catch (NumberFormatException e) {
                                errors.add("Ligne %d : priorité invalide '%s', 0 utilisé".formatted(rowNum, pStr));
                            }
                        }
                    }

                    // Construction des inputs (colonnes dynamiques non vides)
                    Map<String, String> inputs = new LinkedHashMap<>();
                    for (String col : inputColumns) {
                        String val = getCellValue(row, headers.indexOf(col), formatter);
                        if (!val.isBlank()) {
                            inputs.put(col, val);
                        }
                    }

                    TranscoRule rule = new TranscoRule();
                    rule.setContext(context);
                    rule.setOutputValue(outputValue);
                    rule.setPriority(priority);
                    rule.setInputs(objectMapper.valueToTree(inputs));

                    try {
                        repository.save(rule);
                        inserted++;
                    } catch (DataIntegrityViolationException e) {
                        skipped++;
                        log.debug("Ligne {} : doublon ignoré (context={}, inputs={})", rowNum, context, inputs);
                    }

                } catch (Exception e) {
                    errors.add("Ligne %d : erreur inattendue — %s".formatted(rowNum, e.getMessage()));
                    rejected++;
                }
            }

        } catch (Exception e) {
            log.error("Erreur lecture fichier Excel", e);
            return new ImportResultDto(0, 0, 0, List.of("Impossible de lire le fichier : " + e.getMessage()));
        }

        return new ImportResultDto(inserted, skipped, rejected, errors);
    }

    private String getCellValue(Row row, int idx, DataFormatter formatter) {
        if (idx < 0 || idx >= row.getLastCellNum()) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) return false;
        }
        return true;
    }
}
