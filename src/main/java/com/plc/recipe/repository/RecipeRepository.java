package com.plc.recipe.repository;

import com.plc.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByName(String name);

    List<Recipe> findByIsActiveTrue();

    List<Recipe> findByIsActiveTrueOrderByNameAsc();
}
