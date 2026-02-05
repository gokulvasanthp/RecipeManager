package com.plc.recipe.service;

import com.plc.recipe.dto.RecipeDTO;
import com.plc.recipe.entity.Recipe;
import com.plc.recipe.entity.Ingredient;
import com.plc.recipe.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public RecipeDTO createRecipe(RecipeDTO recipeDTO) {
        log.info("Creating new recipe: {}", recipeDTO.getName());

        // Check if recipe with same name exists
        if (recipeRepository.findByName(recipeDTO.getName()).isPresent()) {
            throw new IllegalArgumentException("Recipe with name '" + recipeDTO.getName() + "' already exists");
        }

        Recipe recipe = Recipe.builder()
                .name(recipeDTO.getName())
                .description(recipeDTO.getDescription())
                .batchSize(recipeDTO.getBatchSize())
                .unit(recipeDTO.getUnit())
                .isActive(recipeDTO.getIsActive() != null ? recipeDTO.getIsActive() : true)
                .build();

        // Add ingredients
        if (recipeDTO.getIngredients() != null && !recipeDTO.getIngredients().isEmpty()) {
            List<Ingredient> ingredients = recipeDTO.getIngredients().stream()
                    .map(ingredientDTO -> Ingredient.builder()
                            .name(ingredientDTO.getName())
                            .quantity(ingredientDTO.getQuantity())
                            .unit(ingredientDTO.getUnit())
                            .notes(ingredientDTO.getNotes())
                            .sequenceOrder(ingredientDTO.getSequenceOrder())
                            .recipe(recipe)
                            .build())
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("Recipe created successfully with ID: {}", savedRecipe.getId());

        return mapToDTO(savedRecipe);
    }

    public RecipeDTO getRecipeById(Long id) {
        log.info("Fetching recipe with ID: {}", id);
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + id));
        return mapToDTO(recipe);
    }

    public List<RecipeDTO> getAllRecipes() {
        log.info("Fetching all recipes");
        return recipeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<RecipeDTO> getActiveRecipes() {
        log.info("Fetching active recipes");
        return recipeRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public RecipeDTO updateRecipe(Long id, RecipeDTO recipeDTO) {
        log.info("Updating recipe with ID: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + id));

        recipe.setName(recipeDTO.getName());
        recipe.setDescription(recipeDTO.getDescription());
        recipe.setBatchSize(recipeDTO.getBatchSize());
        recipe.setUnit(recipeDTO.getUnit());
        recipe.setIsActive(recipeDTO.getIsActive());

        // Update ingredients if provided
        if (recipeDTO.getIngredients() != null) {
            recipe.getIngredients().clear();
            List<Ingredient> ingredients = recipeDTO.getIngredients().stream()
                    .map(ingredientDTO -> Ingredient.builder()
                            .name(ingredientDTO.getName())
                            .quantity(ingredientDTO.getQuantity())
                            .unit(ingredientDTO.getUnit())
                            .notes(ingredientDTO.getNotes())
                            .sequenceOrder(ingredientDTO.getSequenceOrder())
                            .recipe(recipe)
                            .build())
                    .collect(Collectors.toList());
            recipe.setIngredients(ingredients);
        }

        Recipe updatedRecipe = recipeRepository.save(recipe);
        log.info("Recipe updated successfully with ID: {}", id);

        return mapToDTO(updatedRecipe);
    }

    public void deleteRecipe(Long id) {
        log.info("Deleting recipe with ID: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + id));

        recipeRepository.delete(recipe);
        log.info("Recipe deleted successfully with ID: {}", id);
    }

    public void activateRecipe(Long id) {
        log.info("Activating recipe with ID: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + id));

        recipe.setIsActive(true);
        recipeRepository.save(recipe);
    }

    public void deactivateRecipe(Long id) {
        log.info("Deactivating recipe with ID: {}", id);

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + id));

        recipe.setIsActive(false);
        recipeRepository.save(recipe);
    }

    private RecipeDTO mapToDTO(Recipe recipe) {
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .batchSize(recipe.getBatchSize())
                .unit(recipe.getUnit())
                .isActive(recipe.getIsActive())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .ingredients(recipe.getIngredients() != null ?
                        recipe.getIngredients().stream()
                                .map(ing -> com.plc.recipe.dto.IngredientDTO.builder()
                                        .id(ing.getId())
                                        .name(ing.getName())
                                        .quantity(ing.getQuantity())
                                        .unit(ing.getUnit())
                                        .notes(ing.getNotes())
                                        .sequenceOrder(ing.getSequenceOrder())
                                        .build())
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
