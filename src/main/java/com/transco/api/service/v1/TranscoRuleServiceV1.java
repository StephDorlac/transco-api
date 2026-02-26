package com.transco.api.service.v1;

import com.transco.api.dto.v1.TranscoRuleDtoV1;

import java.util.List;

public interface TranscoRuleServiceV1 {

    TranscoRuleDtoV1.TranscoRuleResponse create(TranscoRuleDtoV1.TranscoRuleRequest request);

    TranscoRuleDtoV1.TranscoRuleResponse getById(Long id);

    List<TranscoRuleDtoV1.TranscoRuleResponse> getAll();

    List<TranscoRuleDtoV1.TranscoRuleResponse> getByContext(String context);

    TranscoRuleDtoV1.TranscoRuleResponse update(Long id, TranscoRuleDtoV1.TranscoRuleRequest request);

    void delete(Long id);

    TranscoRuleDtoV1.TranscoResolveResponse resolve(TranscoRuleDtoV1.TranscoResolveRequest request);
}
