package com.transco.api.mapper.v1;

import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.entity.TranscoRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TranscoRuleMapperV1 {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TranscoRule toEntity(TranscoRuleDtoV1.TranscoRuleRequest request);

    TranscoRuleDtoV1.TranscoRuleResponse toResponse(TranscoRule entity);

    List<TranscoRuleDtoV1.TranscoRuleResponse> toResponseList(List<TranscoRule> entities);
}
