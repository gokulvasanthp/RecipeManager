package com.plc.recipe.controller;

import com.plc.recipe.dto.RecipeDTO;
import com.plc.recipe.service.RecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeDTO> createRecipe(@Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("REST request to create recipe: {}", recipeDTO.getName());
        RecipeDTO createdRecipe = recipeService.createRecipe(recipeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDTO> getRecipeById(@PathVariable Long id) {
        log.info("REST request to get recipe by ID: {}", id);
        RecipeDTO recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDTO>> getAllRecipes() {
        log.info("REST request to get all recipes");
        List<RecipeDTO> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecipeDTO>> getActiveRecipes() {
        log.info("REST request to get active recipes");
        List<RecipeDTO> recipes = recipeService.getActiveRecipes();
        return ResponseEntity.ok(recipes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDTO> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("REST request to update recipe: {}", id);
        RecipeDTO updatedRecipe = recipeService.updateRecipe(id, recipeDTO);
        return ResponseEntity.ok(updatedRecipe);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        log.info("REST request to delete recipe: {}", id);
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateRecipe(@PathVariable Long id) {
        log.info("REST request to activate recipe: {}", id);
        recipeService.activateRecipe(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateRecipe(@PathVariable Long id) {
        log.info("REST request to deactivate recipe: {}", id);
        recipeService.deactivateRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
