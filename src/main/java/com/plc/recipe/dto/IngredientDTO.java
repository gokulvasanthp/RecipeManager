package com.plc.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {

    private Long id;

    @NotBlank(message = "Ingredient name is required")
    private String name;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    private String notes;

    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;
}
