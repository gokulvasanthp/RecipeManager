package com.plc.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDTO {

    private Long id;

    @NotBlank(message = "Recipe name is required")
    private String name;

    private String description;

    @NotNull(message = "Batch size is required")
    private Double batchSize;

    @NotBlank(message = "Unit is required")
    private String unit;

    @Builder.Default
    private Boolean isActive = true;

    @Valid
    private List<IngredientDTO> ingredients;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
