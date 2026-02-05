package com.plc.recipe.service;

import com.plc.recipe.dto.BatchRunDTO;
import com.plc.recipe.entity.BatchRun;
import com.plc.recipe.entity.Recipe;
import com.plc.recipe.repository.BatchRunRepository;
import com.plc.recipe.repository.RecipeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class BatchRunService {

    private final BatchRunRepository batchRunRepository;
    private final RecipeRepository recipeRepository;
    
    @Autowired
    private EthernetIPService ethernetIPService;
    
    @Autowired
    private RecipeEtherNetIPService recipeEtherNetIPService;

    public BatchRunService(BatchRunRepository batchRunRepository, RecipeRepository recipeRepository) {
        this.batchRunRepository = batchRunRepository;
        this.recipeRepository = recipeRepository;
    }

    public BatchRunDTO createBatchRun(BatchRunDTO batchRunDTO) {
        log.info("Creating new batch run: {}", batchRunDTO.getBatchNumber());

        Recipe recipe = recipeRepository.findById(batchRunDTO.getRecipeId())
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + batchRunDTO.getRecipeId()));

        if (batchRunRepository.findByBatchNumber(batchRunDTO.getBatchNumber()).isPresent()) {
            throw new IllegalArgumentException("Batch with number '" + batchRunDTO.getBatchNumber() + "' already exists");
        }

        BatchRun batchRun = BatchRun.builder()
                .recipe(recipe)
                .batchNumber(batchRunDTO.getBatchNumber())
                .targetQuantity(batchRunDTO.getTargetQuantity())
                .notes(batchRunDTO.getNotes())
                .operatorName(batchRunDTO.getOperatorName())
                .build();

        BatchRun savedBatchRun = batchRunRepository.save(batchRun);
        log.info("Batch run created successfully with ID: {}", savedBatchRun.getId());

        return mapToDTO(savedBatchRun);
    }

    public BatchRunDTO getBatchRunById(Long id) {
        log.info("Fetching batch run with ID: {}", id);
        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));
        return mapToDTO(batchRun);
    }

    public List<BatchRunDTO> getAllBatchRuns() {
        log.info("Fetching all batch runs");
        return batchRunRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BatchRunDTO> getBatchRunsByRecipe(Long recipeId) {
        log.info("Fetching batch runs for recipe ID: {}", recipeId);

        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + recipeId));

        return batchRunRepository.findByRecipeIdOrderByStartedAtDesc(recipeId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BatchRunDTO updateBatchRunStatus(Long id, String status) {
        log.info("Updating batch run {} status to: {}", id, status);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        batchRun.setStatus(BatchRun.BatchStatus.valueOf(status));

        if (BatchRun.BatchStatus.COMPLETED.toString().equals(status)) {
            batchRun.setCompletedAt(LocalDateTime.now());
        }

        BatchRun updatedBatchRun = batchRunRepository.save(batchRun);
        log.info("Batch run status updated successfully");

        return mapToDTO(updatedBatchRun);
    }

    public BatchRunDTO completeBatchRun(Long id, Double actualQuantity) {
        log.info("Completing batch run with ID: {} with actual quantity: {}", id, actualQuantity);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        batchRun.setActualQuantity(actualQuantity);
        batchRun.setStatus(BatchRun.BatchStatus.COMPLETED);
        batchRun.setCompletedAt(LocalDateTime.now());

        BatchRun updatedBatchRun = batchRunRepository.save(batchRun);
        log.info("Batch run completed successfully");

        return mapToDTO(updatedBatchRun);
    }

    /**
     * Start batch run - sends start command to OpENer simulator via EtherNet/IP
     */
    public BatchRunDTO startBatchRun(Long id) {
        log.info("Starting batch run with ID: {}", id);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        // Write recipe to PLC
        boolean recipeWriteSuccess = recipeEtherNetIPService.writeRecipeToPLC(batchRun.getRecipe(), batchRun);
        if (!recipeWriteSuccess) {
            log.warn("Failed to write recipe to PLC, but continuing");
        }

        // Send start command to PLC
        boolean startSuccess = ethernetIPService.sendBatchStart(batchRun.getBatchNumber(), batchRun.getTargetQuantity());
        if (!startSuccess) {
            log.warn("Failed to send start command to PLC");
            batchRun.setStatus(BatchRun.BatchStatus.FAILED);
            return mapToDTO(batchRunRepository.save(batchRun));
        }

        batchRun.setStatus(BatchRun.BatchStatus.RUNNING);
        BatchRun updatedBatchRun = batchRunRepository.save(batchRun);
        
        log.info("Batch run started successfully, status: RUNNING");
        return mapToDTO(updatedBatchRun);
    }

    /**
     * Stop batch run - sends stop command to OpENer simulator via EtherNet/IP
     */
    public BatchRunDTO stopBatchRun(Long id) {
        log.info("Stopping batch run with ID: {}", id);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        // Send stop command to PLC
        boolean stopSuccess = ethernetIPService.sendBatchStop(batchRun.getBatchNumber());
        if (!stopSuccess) {
            log.warn("Failed to send stop command to PLC");
        }

        batchRun.setStatus(BatchRun.BatchStatus.COMPLETED);
        batchRun.setCompletedAt(LocalDateTime.now());
        
        BatchRun updatedBatchRun = batchRunRepository.save(batchRun);
        log.info("Batch run stopped successfully");
        
        return mapToDTO(updatedBatchRun);
    }

    /**
     * Get batch progress from OpENer simulator via EtherNet/IP
     * Reads current status from Assembly 100
     */
    public BatchProgress getBatchProgress(Long id) {
        log.info("Reading batch progress for ID: {}", id);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        // Read progress from PLC
        EthernetIPService.BatchProgress plcProgress = ethernetIPService.getBatchProgress(batchRun.getBatchNumber());
        
        if (plcProgress == null) {
            log.warn("Failed to read progress from PLC, returning database values");
            return new BatchProgress(
                batchRun.getActualQuantity(),
                batchRun.getStatus().toString()
            );
        }

        // Update database with latest values from PLC
        batchRun.setActualQuantity(plcProgress.currentQuantity);
        if ("COMPLETED".equals(plcProgress.status)) {
            batchRun.setStatus(BatchRun.BatchStatus.COMPLETED);
            batchRun.setCompletedAt(LocalDateTime.now());
        }
        batchRunRepository.save(batchRun);

        return new BatchProgress(
            plcProgress.currentQuantity,
            plcProgress.status,
            plcProgress.progressPercentage,
            plcProgress.elapsedSeconds
        );
    }

    public void deleteBatchRun(Long id) {
        log.info("Deleting batch run with ID: {}", id);

        BatchRun batchRun = batchRunRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch run not found with ID: " + id));

        batchRunRepository.delete(batchRun);
        log.info("Batch run deleted successfully");
    }

    private BatchRunDTO mapToDTO(BatchRun batchRun) {
        return BatchRunDTO.builder()
                .id(batchRun.getId())
                .recipeId(batchRun.getRecipe().getId())
                .batchNumber(batchRun.getBatchNumber())
                .targetQuantity(batchRun.getTargetQuantity())
                .actualQuantity(batchRun.getActualQuantity())
                .status(batchRun.getStatus().toString())
                .startedAt(batchRun.getStartedAt())
                .completedAt(batchRun.getCompletedAt())
                .notes(batchRun.getNotes())
                .operatorName(batchRun.getOperatorName())
                .build();
    }

    /**
     * Data class for batch progress information
     */
    public static class BatchProgress {
        public final Double currentQuantity;
        public final String status;
        public final Double progressPercentage;
        public final Integer elapsedSeconds;

        public BatchProgress(Double currentQuantity, String status) {
            this(currentQuantity, status, 0.0, 0);
        }

        public BatchProgress(Double currentQuantity, String status, Double progressPercentage, Integer elapsedSeconds) {
            this.currentQuantity = currentQuantity;
            this.status = status;
            this.progressPercentage = progressPercentage;
            this.elapsedSeconds = elapsedSeconds;
        }

        @Override
        public String toString() {
            return "BatchProgress{" +
                    "currentQuantity=" + currentQuantity +
                    ", status='" + status + '\'' +
                    ", progressPercentage=" + progressPercentage +
                    ", elapsedSeconds=" + elapsedSeconds +
                    '}';
        }
    }
}
