package com.transco.api.dto.v1;

import java.util.List;

public record ImportResultDto(
        int inserted,
        int skipped,
        int rejected,
        List<String> errors
) {}
