package com.plc.recipe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private Double targetQuantity;

    @Column
    private Double actualQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status; // PENDING, RUNNING, COMPLETED, FAILED

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column
    private String operatorName;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        status = BatchStatus.PENDING;
    }

    public enum BatchStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }
}
