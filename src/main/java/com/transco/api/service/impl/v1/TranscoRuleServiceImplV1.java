package com.transco.api.service.impl.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transco.api.dto.v1.TranscoRuleDtoV1;
import com.transco.api.entity.TranscoRule;
import com.transco.api.exception.ResourceAlreadyExistsException;
import com.transco.api.exception.ResourceNotFoundException;
import com.transco.api.mapper.v1.TranscoRuleMapperV1;
import com.transco.api.repository.TranscoRuleRepository;
import com.transco.api.service.v1.TranscoRuleServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscoRuleServiceImplV1 implements TranscoRuleServiceV1 {

    private final TranscoRuleRepository repository;
    private final TranscoRuleMapperV1 mapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public TranscoRuleDtoV1.TranscoRuleResponse create(TranscoRuleDtoV1.TranscoRuleRequest request) {
        if (repository.existsByContextAndInputs(request.context(), request.inputs())) {
            throw new ResourceAlreadyExistsException(
                    "Une règle existe déjà pour le contexte '%s' avec ces inputs".formatted(request.context())
            );
        }
        TranscoRule entity = mapper.toEntity(request);
        TranscoRule saved = repository.save(entity);
        log.info("Règle créée : id={}, context={}", saved.getId(), saved.getContext());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TranscoRuleDtoV1.TranscoRuleResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TranscoRuleDtoV1.TranscoRuleResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TranscoRuleDtoV1.TranscoRuleResponse> getByContext(String context) {
        return mapper.toResponseList(repository.findByContext(context));
    }

    @Override
    @Transactional
    public TranscoRuleDtoV1.TranscoRuleResponse update(Long id, TranscoRuleDtoV1.TranscoRuleRequest request) {
        TranscoRule existing = findOrThrow(id);
        existing.setContext(request.context());
        existing.setInputs(request.inputs());
        existing.setOutputValue(request.outputValue());
        existing.setPriority(request.priority());
        TranscoRule saved = repository.save(existing);
        log.info("Règle mise à jour : id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        TranscoRule rule = findOrThrow(id);
        repository.delete(rule);
        log.info("Règle supprimée : id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public TranscoRuleDtoV1.TranscoResolveResponse resolve(TranscoRuleDtoV1.TranscoResolveRequest request) {
        String inputsJson = toJson(request.inputs());

        Optional<TranscoRule> match = request.withFallback()
                ? repository.resolveWithFallback(request.context(), inputsJson)
                : repository.resolveExact(request.context(), inputsJson);

        if (match.isPresent()) {
            TranscoRule rule = match.get();
            log.info("Règle résolue : context={}, outputValue={}, priority={}", request.context(), rule.getOutputValue(), rule.getPriority());
            return new TranscoRuleDtoV1.TranscoResolveResponse(
                    request.context(),
                    request.inputs(),
                    rule.getOutputValue(),
                    rule.getPriority(),
                    true
            );
        }

        log.warn("Aucune règle trouvée pour context={}, inputs={}", request.context(), inputsJson);
        return new TranscoRuleDtoV1.TranscoResolveResponse(
                request.context(),
                request.inputs(),
                null,
                null,
                false
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private TranscoRule findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Règle introuvable : id=" + id));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Impossible de sérialiser les inputs en JSON", e);
        }
    }
}
