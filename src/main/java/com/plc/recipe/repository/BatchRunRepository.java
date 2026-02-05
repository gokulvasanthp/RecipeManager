package com.plc.recipe.repository;

import com.plc.recipe.entity.BatchRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRunRepository extends JpaRepository<BatchRun, Long> {

    Optional<BatchRun> findByBatchNumber(String batchNumber);

    List<BatchRun> findByRecipeIdOrderByStartedAtDesc(Long recipeId);

    List<BatchRun> findByStatusOrderByStartedAtDesc(String status);
}
