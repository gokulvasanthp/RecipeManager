# OpENer EtherNet/IP Integration Guide

## Overview

The Spring Boot application has been configured to communicate with the OpENer PLC simulator via EtherNet/IP protocol on port 44818. This enables real-time reading and writing of batch recipe data.

## Configuration

### Application Properties

Edit `src/main/resources/application.properties`:

```properties
# PLC EtherNet/IP Configuration
plc.host=localhost              # OpENer simulator host
plc.port=44818                  # OpENer EtherNet/IP port
plc.offline-mode=true           # Start in offline mode (safe)
plc.connection-timeout=5000     # Connection timeout in ms
```

### Enable Online Mode

To connect to the actual OpENer simulator:

```properties
plc.offline-mode=false          # Enable online mode
plc.host=localhost              # Or IP of docker host if remote
```

## Services

### EthernetIPService

Core service handling low-level EtherNet/IP communication:

**Key Methods:**
- `initializeConnection()` - Connect to OpENer simulator
- `closeConnection()` - Disconnect from simulator
- `sendBatchStart(batchNumber, quantity)` - Write START command to Assembly 150
- `sendBatchStop(batchNumber)` - Write STOP command to Assembly 150
- `getBatchProgress(batchNumber)` - Read status from Assembly 100
- `isPLCConnected()` - Check connection status

**Usage Example:**
```java
@Autowired
private EthernetIPService ethernetIPService;

// Enable online mode
ethernetIPService.setOfflineMode(false);

// Send batch start command
boolean success = ethernetIPService.sendBatchStart("BATCH001", 100.0);

// Read batch progress
EthernetIPService.BatchProgress progress = 
    ethernetIPService.getBatchProgress("BATCH001");
```

### RecipeEtherNetIPService

High-level service for recipe-specific operations:

**Key Methods:**
- `writeRecipeToPLC(recipe, batchRun)` - Write complete recipe to PLC
- `readRecipeFromPLC()` - Read recipe data from PLC
- `updateBatchProgressOnPLC(...)` - Update batch progress on PLC
- `isBatchCompleteOnPLC()` - Check if batch finished
- `getRecipeMetricsFromPLC()` - Get performance metrics

**Usage Example:**
```java
@Autowired
private RecipeEtherNetIPService recipeService;

// Write recipe to PLC before starting batch
recipeService.writeRecipeToPLC(recipe, batchRun);

// Get recipe metrics during execution
Map<String, Object> metrics = recipeService.getRecipeMetricsFromPLC();
```

### BatchRunService

Service integrating EtherNet/IP with batch management:

**Key Methods:**
- `startBatchRun(id)` - Start batch and write recipe to PLC
- `stopBatchRun(id)` - Stop batch and halt PLC simulation
- `getBatchProgress(id)` - Read progress from PLC and update database

## API Endpoints

### Batch Control

**Start Batch**
```bash
POST /api/batch-runs/{id}/start

# Response (202 Accepted)
{
  "message": "Batch started successfully",
  "batchNumber": "BATCH001",
  "status": "RUNNING",
  "id": 1
}
```

**Stop Batch**
```bash
POST /api/batch-runs/{id}/stop

# Response (200 OK)
{
  "message": "Batch stopped successfully",
  "batchNumber": "BATCH001",
  "status": "COMPLETED",
  "id": 1
}
```

### Progress Monitoring

**Get Batch Progress**
```bash
GET /api/batch-runs/{id}/progress

# Response (200 OK)
{
  "id": 1,
  "batchNumber": "BATCH001",
  "targetQuantity": 100.0,
  "currentQuantity": 75.5,
  "status": "RUNNING",
  "progressPercentage": 75.5,
  "elapsedSeconds": 37
}
```

**Get Recipe Metrics**
```bash
GET /api/batch-runs/{id}/recipe-metrics

# Response (200 OK)
{
  "currentQuantity": 75.5,
  "progressPercentage": 75.5,
  "elapsedSeconds": 37,
  "efficiency": 2.04
}
```

### PLC Status

**Check PLC Connection**
```bash
GET /api/batch-runs/plc/status

# Response (200 OK)
{
  "connected": true,
  "offlineMode": false,
  "status": "CONNECTED"
}
```

**Toggle Offline Mode**
```bash
POST /api/batch-runs/plc/mode

# Request Body
{
  "offlineMode": false
}

# Response (200 OK)
{
  "message": "PLC mode changed",
  "offlineMode": false,
  "mode": "ONLINE"
}
```

## Assembly Mapping

### Assembly 100 (Input - PLC → Application)

Batch status data read from OpENer:

| Offset | Type | Size | Description |
|--------|------|------|-------------|
| 0-3 | int32 | 4 | RecipeID |
| 4-7 | int32 | 4 | BatchID |
| 8 | uint8 | 1 | BatchStatus (0=IDLE, 1=RUNNING, 2=COMPLETED, 3=FAILED) |
| 9 | uint8 | 1 | OperationMode (0=Offline, 1=Online) |
| 10-13 | float | 4 | ActualQuantity |
| 14-17 | float | 4 | ProgressPercentage (0-100) |
| 18-21 | int32 | 4 | ElapsedTime (seconds) |

### Assembly 150 (Output - Application → PLC)

Control commands sent to OpENer:

| Offset | Type | Size | Description |
|--------|------|------|-------------|
| 0 | uint8 | 1 | StartCmd (1 = start batch) |
| 1 | uint8 | 1 | StopCmd (1 = stop batch) |
| 2 | uint8 | 1 | AckCmd (acknowledgment) |
| 3-7 | reserved | 5 | Reserved for future use |

## Data Flow

### Start Batch Flow

```
UI: POST /api/batch-runs/{id}/start
    ↓
BatchRunController.startBatch()
    ↓
BatchRunService.startBatchRun()
    ├─ RecipeEtherNetIPService.writeRecipeToPLC()
    │   └─ EthernetIPService: Write recipe to Assembly 150
    ├─ EthernetIPService.sendBatchStart()
    │   └─ Write START command to Assembly 150
    └─ Update BatchRun status to RUNNING
    ↓
OpENer Simulator: Read Assembly 150 → Start batch
    ↓
Response: {"status": "RUNNING", ...}
```

### Get Progress Flow

```
UI: GET /api/batch-runs/{id}/progress
    ↓
BatchRunController.getBatchProgress()
    ↓
BatchRunService.getBatchProgress()
    ├─ EthernetIPService.getBatchProgress()
    │   └─ Read Assembly 100 from OpENer
    └─ Update BatchRun with latest values
    ↓
Response: {"status": "RUNNING", "progressPercentage": 75.5, ...}
```

## Testing

### Test with cURL

**Enable online mode:**
```bash
curl -X POST http://localhost:8080/api/batch-runs/plc/mode \
  -H "Content-Type: application/json" \
  -d '{"offlineMode": false}'
```

**Create batch:**
```bash
curl -X POST http://localhost:8080/api/batch-runs \
  -H "Content-Type: application/json" \
  -d '{
    "recipeId": 1,
    "batchNumber": "TEST001",
    "targetQuantity": 100,
    "operatorName": "John Doe"
  }'
```

**Start batch:**
```bash
curl -X POST http://localhost:8080/api/batch-runs/1/start
```

**Monitor progress:**
```bash
curl -X GET http://localhost:8080/api/batch-runs/1/progress
```

**Stop batch:**
```bash
curl -X POST http://localhost:8080/api/batch-runs/1/stop
```

## Troubleshooting

### Connection Issues

**"Failed to connect to PLC"**
- Verify OpENer container is running: `docker ps | grep opener-plc`
- Verify port 44818 is accessible: `netstat -an | grep 44818`
- Check firewall allows port 44818

**"Offline mode - using simulated responses"**
- Check `plc.offline-mode` property
- Ensure `plc.offline-mode=false` to enable online mode
- Restart Spring Boot after changing property

### Data Issues

**Progress not updating**
- Check OpENer logs: `docker logs opener-plc -f`
- Verify batch was started: Check response contains `"status": "RUNNING"`
- Check connection status endpoint: `GET /api/batch-runs/plc/status`

**Assembly read returns zeros**
- OpENer simulator may not be running batch
- Start batch using REST API first
- Check Assembly 100 mapping in sample_application.c

## Performance Considerations

- Connection timeout: 5 seconds (configurable)
- Read/Write operations: Synchronous (blocking)
- No connection pooling (creates new session per operation)
- Consider async implementation for high-frequency operations

## Future Enhancements

1. **Connection Pooling**: Reuse client connections
2. **Async Operations**: Use CompletableFuture for non-blocking I/O
3. **Batch Operations**: Group multiple reads/writes
4. **Retry Logic**: Automatic reconnection on failure
5. **Caching**: Cache frequently read values
6. **WebSocket**: Real-time bidirectional updates
7. **Advanced CIP**: Support more complex data types

## References

- **OpENer GitHub**: https://github.com/EIPStackGroup/OpENer
- **digitalpetri ENIP**: https://github.com/digitalpetri/enip-core
- **EtherNet/IP Spec**: https://www.odva.org/
- **CIP Specification**: https://www.odva.org/

---

**Configuration Status**: ✅ Complete
**Integration Status**: ✅ Ready for Testing
**Online Mode**: Disabled by default (safe)
