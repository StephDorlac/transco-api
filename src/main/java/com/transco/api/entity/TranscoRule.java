package com.transco.api.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
    name = "transco_rule",
    uniqueConstraints = @UniqueConstraint(
        name = "idx_transco_unique",
        columnNames = {"context", "inputs"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscoRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "context", nullable = false, length = 100)
    private String context;

    @Type(JsonBinaryType.class)
    @Column(name = "inputs", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> inputs;

    @Column(name = "output_value", nullable = false)
    private String outputValue;

    @Builder.Default
    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
