package com.plc.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchRunDTO {

    private Long id;

    @NotNull(message = "Recipe ID is required")
    private Long recipeId;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotNull(message = "Target quantity is required")
    @Positive(message = "Target quantity must be positive")
    private Double targetQuantity;

    private Double actualQuantity;

    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private String notes;

    private String operatorName;
}
