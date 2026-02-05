package com.plc.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ingredient name is required")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Column(nullable = false)
    private Double quantity;

    @NotBlank(message = "Unit is required")
    @Column(nullable = false)
    private String unit; // e.g., kg, liter, grams, ml

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private Integer sequenceOrder; // Order of ingredient in recipe
}
