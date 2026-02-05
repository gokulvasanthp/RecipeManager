package com.plc.recipe.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Service for communicating with PLC devices via Ethernet/IP protocol.
 * Connects to OpENer simulator on port 44818 to read/write batch data.
 * 
 * Current implementation: Offline simulation mode
 * - Simulates batch processing with configurable duration
 * - Maintains state in memory for testing
 * 
 * Future implementation: Online mode
 * - Will use proper EtherNet/IP library for actual PLC communication
 * - Assembly 100: Input (PLC → Application) - Batch status, progress, quantity
 * - Assembly 150: Output (Application → PLC) - Batch control commands
 */
@Service
@Slf4j
public class EthernetIPService {

    @Value("${plc.host:localhost}")
    private String plcHost;

    @Value("${plc.port:44818}")
    private int plcPort;

    @Value("${plc.offline-mode:true}")
    private boolean offlineMode;

    // Simulated batch state storage
    private final ConcurrentHashMap<String, SimulatedBatchState> batchStates = new ConcurrentHashMap<>();
    
    private static final long BATCH_DURATION_MS = 60000; // 60 seconds
    private static final int ASSEMBLY_INPUT = 100;   // PLC → App (read)
    private static final int ASSEMBLY_OUTPUT = 150;  // App → PLC (write)

    public EthernetIPService() {
        log.info("EthernetIPService initialized (Offline Simulation Mode)");
    }

    /**
     * Initialize connection to OpENer simulator
     * In offline mode, this just logs the configuration
     */
    public synchronized boolean initializeConnection() {
        log.info("Initializing EtherNet/IP connection to PLC at {}:{}", plcHost, plcPort);
        
        if (offlineMode) {
            log.info("Running in OFFLINE MODE - using simulated batch responses");
            return true;
        }

        // TODO: Implement actual EtherNet/IP client connection when proper library is available
        // This would:
        // 1. Create EtherNet/IP session to PLC
        // 2. Establish connection on port 44818
        // 3. Register session
        // 4. Ready for assembly read/write operations
        
        log.warn("Online mode not yet implemented - falling back to offline simulation");
        return true;
    }

    /**
     * Close connection to OpENer simulator
     */
    public synchronized void closeConnection() {
        log.info("Closing EtherNet/IP connection to PLC");
        // TODO: Implement actual connection cleanup
    }

    /**
     * Send batch start command to OpENer simulator
     * In offline mode, creates a simulated batch in progress state
     */
    public boolean sendBatchStart(String batchNumber, Double quantity) {
        log.info("Sending batch start command: {} with quantity: {}", batchNumber, quantity);

        if (offlineMode) {
            log.info("Offline mode - Starting simulated batch: {}", batchNumber);
            SimulatedBatchState state = new SimulatedBatchState(quantity);
            batchStates.put(batchNumber, state);
            return true;
        }

        // TODO: Online mode implementation
        // Would send CIP Write command to Assembly 150 with start signal
        return true;
    }

    /**
     * Send batch stop command to OpENer simulator
     * In offline mode, marks the batch as stopped
     */
    public boolean sendBatchStop(String batchNumber) {
        log.info("Sending batch stop command: {}", batchNumber);

        if (offlineMode) {
            log.info("Offline mode - Stopping simulated batch: {}", batchNumber);
            SimulatedBatchState state = batchStates.get(batchNumber);
            if (state != null) {
                state.stop();
            }
            return true;
        }

        // TODO: Online mode implementation
        // Would send CIP Write command to Assembly 150 with stop signal
        return true;
    }

    /**
     * Read current batch progress from OpENer simulator
     * In offline mode, returns simulated progress based on elapsed time
     */
    public BatchProgress getBatchProgress(String batchNumber) {
        log.debug("Reading batch progress for: {}", batchNumber);

        if (offlineMode) {
            log.debug("Offline mode - Simulating batch progress read for: {}", batchNumber);
            return simulateGetBatchProgress(batchNumber);
        }

        // TODO: Online mode implementation
        // Would send CIP Read command to Assembly 100 and parse response
        // Parse assembly data structure:
        // 0-3: RecipeID (int32)
        // 4-7: BatchID (int32)
        // 8: BatchStatus (uint8: 0=IDLE, 1=RUNNING, 2=COMPLETED, 3=FAILED)
        // 9: OperationMode (uint8)
        // 10-13: ActualQuantity (float)
        // 14-17: ProgressPercentage (float)
        // 18-21: ElapsedTime (int32)
        
        return null;
    }

    /**
     * Check if PLC connection is established
     */
    public boolean isPLCConnected() {
        log.debug("Checking PLC connection status");

        if (offlineMode) {
            log.debug("Offline mode - Reporting simulated connection status");
            return true; // Always "connected" in offline mode
        }

        // TODO: Check actual PLC connection status
        return false;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offline) {
        this.offlineMode = offline;
        log.info("EtherNet/IP mode changed to: {}", offline ? "OFFLINE" : "ONLINE");
    }

    // Offline simulation methods

    private boolean simulateBatchStart(String batchNumber, Double quantity) {
        log.debug("Simulating batch start: {} with quantity: {}", batchNumber, quantity);
        SimulatedBatchState state = new SimulatedBatchState(quantity);
        batchStates.put(batchNumber, state);
        return true;
    }

    private boolean simulateBatchStop(String batchNumber) {
        log.debug("Simulating batch stop: {}", batchNumber);
        SimulatedBatchState state = batchStates.get(batchNumber);
        if (state != null) {
            state.stop();
        }
        return true;
    }

    private BatchProgress simulateGetBatchProgress(String batchNumber) {
        log.debug("Simulating batch progress read: {}", batchNumber);
        
        SimulatedBatchState state = batchStates.get(batchNumber);
        if (state == null) {
            return new BatchProgress(0.0, "IDLE", 0.0, 0);
        }

        return state.getProgress();
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

    /**
     * Simulated batch state for offline testing
     * Tracks batch progress over time with linear progression
     */
    private static class SimulatedBatchState {
        private final long startTime;
        private final Double targetQuantity;
        private boolean stopped = false;

        public SimulatedBatchState(Double targetQuantity) {
            this.startTime = Instant.now().toEpochMilli();
            this.targetQuantity = targetQuantity;
        }

        public void stop() {
            this.stopped = true;
        }

        public BatchProgress getProgress() {
            long elapsed = Instant.now().toEpochMilli() - startTime;
            long elapsedSeconds = elapsed / 1000;
            
            // Linear progress: 0-100% over 60 seconds
            double progressPercentage = Math.min(100.0, (elapsedSeconds / 60.0) * 100.0);
            double currentQuantity = (progressPercentage / 100.0) * targetQuantity;

            String status;
            if (stopped) {
                status = "STOPPED";
            } else if (progressPercentage >= 100.0) {
                status = "COMPLETED";
            } else {
                status = "RUNNING";
            }

            return new BatchProgress(
                currentQuantity,
                status,
                progressPercentage,
                (int) elapsedSeconds
            );
        }
    }}