package com.transco.api.service.v1;

import com.transco.api.dto.v1.ImportResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface TranscoImportServiceV1 {
    ImportResultDto importFromExcel(MultipartFile file);
}
