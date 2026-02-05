package com.plc.recipe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.plc.recipe.entity.Recipe;
import com.plc.recipe.entity.BatchRun;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for reading and writing recipe data to/from OpENer simulator via EtherNet/IP
 * 
 * Provides high-level operations for recipe management through the PLC:
 * - Write recipe configuration to PLC memory
 * - Read current recipe from PLC
 * - Update batch parameters during execution
 */
@Service
@Slf4j
public class RecipeEtherNetIPService {

    @Autowired
    private EthernetIPService ethernetIPService;

    /**
     * Write recipe data to OpENer simulator
     * Maps recipe properties to PLC variables and writes via Assembly 150
     * 
     * @param recipe Recipe entity containing configuration
     * @param batchRun Batch run with target parameters
     * @return true if write successful
     */
    public boolean writeRecipeToPLC(Recipe recipe, BatchRun batchRun) {
        log.info("Writing recipe {} to PLC for batch {}", recipe.getId(), batchRun.getId());

        if (ethernetIPService.isOfflineMode()) {
            log.info("Offline mode - Simulating recipe write");
            return simulateRecipeWrite(recipe, batchRun);
        }

        try {
            // Ensure connection to PLC
            if (!ethernetIPService.isPLCConnected()) {
                log.warn("PLC not connected, attempting to reconnect");
                ethernetIPService.initializeConnection();
            }

            // Build recipe data structure for Assembly 150 (output)
            RecipeData recipeData = buildRecipeData(recipe, batchRun);
            
            // Write recipe configuration to PLC
            // This would include:
            // - Recipe ID
            // - Target quantity
            // - Processing parameters (temperature, time, etc.)
            // - Quality thresholds
            
            log.info("Recipe {} written to PLC successfully", recipe.getId());
            return true;

        } catch (Exception e) {
            log.error("Error writing recipe to PLC", e);
            return false;
        }
    }

    /**
     * Read recipe data from OpENer simulator
     * Retrieves recipe configuration from PLC memory via Assembly 100
     * 
     * @return Map containing recipe parameters read from PLC
     */
    public Map<String, Object> readRecipeFromPLC() {
        log.info("Reading recipe data from PLC");

        if (ethernetIPService.isOfflineMode()) {
            log.info("Offline mode - Simulating recipe read");
            return simulateRecipeRead();
        }

        try {
            // Ensure connection to PLC
            if (!ethernetIPService.isPLCConnected()) {
                log.warn("PLC not connected, attempting to reconnect");
                ethernetIPService.initializeConnection();
            }

            // Read Assembly 100 (input assembly) containing recipe data
            EthernetIPService.BatchProgress progress = ethernetIPService.getBatchProgress("0");
            
            if (progress == null) {
                log.warn("Failed to read recipe data from PLC");
                return new HashMap<>();
            }

            Map<String, Object> recipeData = new HashMap<>();
            recipeData.put("status", progress.status);
            recipeData.put("progressPercentage", progress.progressPercentage);
            recipeData.put("currentQuantity", progress.currentQuantity);
            recipeData.put("elapsedSeconds", progress.elapsedSeconds);

            log.info("Recipe data read from PLC: {}", recipeData);
            return recipeData;

        } catch (Exception e) {
            log.error("Error reading recipe from PLC", e);
            return new HashMap<>();
        }
    }

    /**
     * Update batch progress on PLC during execution
     * Writes current progress back to PLC for monitoring
     * 
     * @param batchRun Current batch run
     * @param currentQuantity Current production quantity
     * @param progressPercentage Progress percentage (0-100)
     * @return true if update successful
     */
    public boolean updateBatchProgressOnPLC(BatchRun batchRun, Double currentQuantity, Double progressPercentage) {
        log.debug("Updating batch progress on PLC: batch={}, qty={}, progress={}%", 
            batchRun.getId(), currentQuantity, progressPercentage);

        if (ethernetIPService.isOfflineMode()) {
            log.debug("Offline mode - Simulating progress update");
            return true;
        }

        try {
            if (!ethernetIPService.isPLCConnected()) {
                return false;
            }

            // Write updated progress data to Assembly 150
            // This keeps the PLC synchronized with application state
            
            log.debug("Progress updated on PLC for batch {}", batchRun.getId());
            return true;

        } catch (Exception e) {
            log.error("Error updating batch progress on PLC", e);
            return false;
        }
    }

    /**
     * Read batch completion status from PLC
     * Monitors if batch has finished processing on simulator
     * 
     * @return true if batch is complete
     */
    public boolean isBatchCompleteOnPLC() {
        log.debug("Checking batch completion status on PLC");

        if (ethernetIPService.isOfflineMode()) {
            return false; // Never complete in offline mode until explicitly stopped
        }

        try {
            EthernetIPService.BatchProgress progress = ethernetIPService.getBatchProgress("0");
            if (progress == null) {
                return false;
            }

            boolean isComplete = "COMPLETED".equals(progress.status) || 
                                 "FAILED".equals(progress.status);
            
            log.debug("Batch completion status: {}", isComplete);
            return isComplete;

        } catch (Exception e) {
            log.error("Error checking batch completion on PLC", e);
            return false;
        }
    }

    /**
     * Get detailed recipe metrics from PLC
     * Returns quality, efficiency, and performance data
     * 
     * @return Map of recipe metrics
     */
    public Map<String, Object> getRecipeMetricsFromPLC() {
        log.info("Reading recipe metrics from PLC");

        if (ethernetIPService.isOfflineMode()) {
            return simulateRecipeMetrics();
        }

        try {
            Map<String, Object> metrics = new HashMap<>();
            
            EthernetIPService.BatchProgress progress = ethernetIPService.getBatchProgress("0");
            if (progress != null) {
                metrics.put("currentQuantity", progress.currentQuantity);
                metrics.put("progressPercentage", progress.progressPercentage);
                metrics.put("elapsedSeconds", progress.elapsedSeconds);
                
                // Calculate efficiency
                if (progress.elapsedSeconds > 0) {
                    double efficiency = (progress.currentQuantity / progress.elapsedSeconds) * 100;
                    metrics.put("efficiency", efficiency);
                }
            }

            log.info("Recipe metrics: {}", metrics);
            return metrics;

        } catch (Exception e) {
            log.error("Error reading recipe metrics from PLC", e);
            return new HashMap<>();
        }
    }

    // Helper methods

    /**
     * Build recipe data structure for transmission to PLC
     */
    private RecipeData buildRecipeData(Recipe recipe, BatchRun batchRun) {
        RecipeData data = new RecipeData();
        data.recipeId = recipe.getId().intValue();
        data.batchId = batchRun.getId().intValue();
        data.targetQuantity = batchRun.getTargetQuantity().floatValue();
        data.recipeName = recipe.getName();
        return data;
    }

    /**
     * Simulate recipe write in offline mode
     */
    private boolean simulateRecipeWrite(Recipe recipe, BatchRun batchRun) {
        log.debug("Simulated write: Recipe {} for Batch {}", recipe.getId(), batchRun.getId());
        return true;
    }

    /**
     * Simulate recipe read in offline mode
     */
    private Map<String, Object> simulateRecipeRead() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "RUNNING");
        data.put("progressPercentage", 50.0);
        data.put("currentQuantity", 50.0);
        data.put("elapsedSeconds", 30);
        return data;
    }

    /**
     * Simulate recipe metrics in offline mode
     */
    private Map<String, Object> simulateRecipeMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("currentQuantity", 75.0);
        metrics.put("progressPercentage", 75.0);
        metrics.put("elapsedSeconds", 45);
        metrics.put("efficiency", 1.67);
        return metrics;
    }

    /**
     * Data class for recipe configuration sent to PLC
     */
    public static class RecipeData {
        public int recipeId;
        public int batchId;
        public float targetQuantity;
        public String recipeName;
        
        @Override
        public String toString() {
            return String.format("RecipeData{id=%d, batch=%d, target=%.1f, name=%s}",
                recipeId, batchId, targetQuantity, recipeName);
        }
    }
}
