package com.plc.recipe.controller;

import com.plc.recipe.dto.BatchRunDTO;
import com.plc.recipe.service.BatchRunService;
import com.plc.recipe.service.EthernetIPService;
import com.plc.recipe.service.RecipeEtherNetIPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch-runs")
@Slf4j
public class BatchRunController {

    private final BatchRunService batchRunService;
    private final EthernetIPService ethernetIPService;

    @Autowired
    private RecipeEtherNetIPService recipeEtherNetIPService;

    public BatchRunController(BatchRunService batchRunService, EthernetIPService ethernetIPService) {
        this.batchRunService = batchRunService;
        this.ethernetIPService = ethernetIPService;
    }

    @PostMapping
    public ResponseEntity<BatchRunDTO> createBatchRun(@Valid @RequestBody BatchRunDTO batchRunDTO) {
        log.info("REST request to create batch run: {}", batchRunDTO.getBatchNumber());
        BatchRunDTO createdBatchRun = batchRunService.createBatchRun(batchRunDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBatchRun);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BatchRunDTO> getBatchRunById(@PathVariable Long id) {
        log.info("REST request to get batch run by ID: {}", id);
        BatchRunDTO batchRun = batchRunService.getBatchRunById(id);
        return ResponseEntity.ok(batchRun);
    }

    @GetMapping
    public ResponseEntity<List<BatchRunDTO>> getAllBatchRuns() {
        log.info("REST request to get all batch runs");
        List<BatchRunDTO> batchRuns = batchRunService.getAllBatchRuns();
        return ResponseEntity.ok(batchRuns);
    }

    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<BatchRunDTO>> getBatchRunsByRecipe(@PathVariable Long recipeId) {
        log.info("REST request to get batch runs for recipe: {}", recipeId);
        List<BatchRunDTO> batchRuns = batchRunService.getBatchRunsByRecipe(recipeId);
        return ResponseEntity.ok(batchRuns);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BatchRunDTO> updateBatchRunStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("REST request to update batch run status: {}", id);
        String status = body.get("status");
        BatchRunDTO updatedBatchRun = batchRunService.updateBatchRunStatus(id, status);
        return ResponseEntity.ok(updatedBatchRun);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BatchRunDTO> completeBatchRun(
            @PathVariable Long id,
            @RequestBody Map<String, Double> body) {
        log.info("REST request to complete batch run: {}", id);
        Double actualQuantity = body.get("actualQuantity");
        BatchRunDTO completedBatchRun = batchRunService.completeBatchRun(id, actualQuantity);
        return ResponseEntity.ok(completedBatchRun);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatchRun(@PathVariable Long id) {
        log.info("REST request to delete batch run: {}", id);
        batchRunService.deleteBatchRun(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Map<String, Object>> startBatch(@PathVariable Long id) {
        log.info("REST request to start batch run: {}", id);

        try {
            BatchRunDTO updatedBatchRun = batchRunService.startBatchRun(id);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Batch started successfully",
                    "batchNumber", updatedBatchRun.getBatchNumber(),
                    "status", updatedBatchRun.getStatus(),
                    "id", updatedBatchRun.getId()
            ));
        } catch (Exception e) {
            log.error("Error starting batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to start batch: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Map<String, Object>> stopBatch(@PathVariable Long id) {
        log.info("REST request to stop batch run: {}", id);

        try {
            BatchRunDTO updatedBatchRun = batchRunService.stopBatchRun(id);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Batch stopped successfully",
                    "batchNumber", updatedBatchRun.getBatchNumber(),
                    "status", updatedBatchRun.getStatus(),
                    "id", updatedBatchRun.getId()
            ));
        } catch (Exception e) {
            log.error("Error stopping batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to stop batch: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<Map<String, Object>> getBatchProgress(@PathVariable Long id) {
        log.info("REST request to get batch progress: {}", id);

        try {
            BatchRunDTO batchRun = batchRunService.getBatchRunById(id);
            BatchRunService.BatchProgress progress = batchRunService.getBatchProgress(id);

            return ResponseEntity.ok(Map.of(
                    "id", batchRun.getId(),
                    "batchNumber", batchRun.getBatchNumber(),
                    "targetQuantity", batchRun.getTargetQuantity(),
                    "currentQuantity", progress.currentQuantity,
                    "status", progress.status,
                    "progressPercentage", progress.progressPercentage,
                    "elapsedSeconds", progress.elapsedSeconds
            ));
        } catch (Exception e) {
            log.error("Error getting batch progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get batch progress: " + e.getMessage()));
        }
    }

    /**
     * Get recipe metrics from PLC
     */
    @GetMapping("/{id}/recipe-metrics")
    public ResponseEntity<Map<String, Object>> getRecipeMetrics(@PathVariable Long id) {
        log.info("REST request to get recipe metrics for batch: {}", id);

        try {
            Map<String, Object> metrics = recipeEtherNetIPService.getRecipeMetricsFromPLC();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting recipe metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to get recipe metrics: " + e.getMessage()));
        }
    }

    /**
     * Check PLC connection status
     */
    @GetMapping("/plc-status")
    public ResponseEntity<Map<String, Object>> getPLCStatus() {
        log.info("REST request to check PLC status");

        try {
            boolean connected = ethernetIPService.isPLCConnected();
            boolean offlineMode = ethernetIPService.isOfflineMode();
            
            return ResponseEntity.ok(Map.of(
                    "connected", connected,
                    "offlineMode", offlineMode,
                    "status", connected ? "CONNECTED" : "DISCONNECTED"
            ));
        } catch (Exception e) {
            log.error("Error checking PLC status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to check PLC status: " + e.getMessage()));
        }
    }

    /**
     * Enable/disable offline mode
     */
    @PostMapping("/plc-mode")
    public ResponseEntity<Map<String, Object>> setPLCMode(@RequestBody Map<String, Boolean> body) {
        log.info("REST request to set PLC mode: {}", body);

        try {
            Boolean offlineMode = body.getOrDefault("offlineMode", true);
            ethernetIPService.setOfflineMode(offlineMode);
            
            return ResponseEntity.ok(Map.of(
                    "message", "PLC mode changed",
                    "offlineMode", offlineMode,
                    "mode", offlineMode ? "OFFLINE" : "ONLINE"
            ));
        } catch (Exception e) {
            log.error("Error setting PLC mode", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to set PLC mode: " + e.getMessage()));
        }
    }}