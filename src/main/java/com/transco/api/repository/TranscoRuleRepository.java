package com.transco.api.repository;

import com.transco.api.entity.TranscoRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscoRuleRepository extends JpaRepository<TranscoRule, Long> {

    List<TranscoRule> findByContext(String context);

    /**
     * Résolution avec fallback : trouve toutes les règles dont les inputs
     * sont un sous-ensemble des inputs fournis, ordonnées par priorité décroissante.
     * Utilise l'opérateur JSONB @> (contient).
     */
    @Query(value = """
            SELECT *
            FROM transco_rule
            WHERE context = :context
              AND CAST(:inputsJson AS jsonb) @> inputs
            ORDER BY priority DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<TranscoRule> resolveWithFallback(
            @Param("context") String context,
            @Param("inputsJson") String inputsJson
    );

    /**
     * Résolution exacte : les inputs doivent correspondre exactement.
     */
    @Query(value = """
            SELECT *
            FROM transco_rule
            WHERE context = :context
              AND inputs = CAST(:inputsJson AS jsonb)
            ORDER BY priority DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<TranscoRule> resolveExact(
            @Param("context") String context,
            @Param("inputsJson") String inputsJson
    );

    boolean existsByContextAndInputs(String context, java.util.Map<String, String> inputs);
}
