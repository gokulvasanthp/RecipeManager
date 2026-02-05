# Batch Monitor UI - PLC Simulator Integration

## Overview
The Batch Monitor component displays real-time batch progress from the OpENer PLC simulator via EtherNet/IP Assembly 100. It provides visual feedback on batch execution, quantity tracking, and progress metrics.

## Components

### 1. **batch-monitor.component.ts**
New component that polls PLC simulator data and displays real-time progress.

**Key Features:**
- Polls Assembly 100 data every 2 seconds (configurable)
- Displays progress percentage, elapsed time, and estimated remaining time
- Auto-stops polling when batch completes
- Shows all simulator variables with source annotations
- Refresh button for manual updates

**Inputs:**
- `batchRun: BatchRun` - The batch run to monitor

**Data from Assembly 100:**
- `RecipeID` - Current recipe identifier
- `BatchID` - Current batch identifier
- `BatchStatus` - Batch execution state (0=IDLE, 1=RUNNING, 2=COMPLETED, 3=FAILED)
- `TargetQuantity` - Target batch quantity
- `ActualQuantity` - Current measured quantity (increases with progress)
- `ProgressPercentage` - Batch completion 0-100%
- `ElapsedTime` - Seconds elapsed in batch

### 2. **batch-run-list.component.ts** (Updated)
Enhanced to integrate batch-monitor component and show simulator data.

**Changes:**
- Still loads and manages batch runs
- Renders batch-monitor for each batch
- Shows quick stats: Target Qty, Actual Qty, Operator
- Assembly information legend added
- Start/Stop buttons trigger Assembly 150 commands

### 3. **batch-run-list.component.html** (Updated)
Restructured to card-based layout with embedded monitoring.

**Layout:**
```
┌─ Batch Item Card
├─ Batch Summary (Quick Stats)
├─ Batch Monitor (Real-time Progress from Assembly 100)
├─ Action Buttons (Sends Commands to Assembly 150)
└─ Started Time
```

## EtherNet/IP Integration

### Assembly 100 Data Flow
```
PLC Simulator                   Angular UI
    ↓
Write Assembly 100 (24 bytes)
    ↓
RecipeID, BatchID, Status, 
TargetQuantity, ActualQuantity,
ProgressPercentage, ElapsedTime
    ↓
Spring Boot Backend (polls)
    ↓
HTTP GET /api/batch-runs/{id}
    ↓
batch-monitor.component polls
every 2 seconds
    ↓
Updates UI with real-time
progress, quantity, timing
```

### Assembly 150 Data Flow
```
Angular UI (User Action)
    ↓
Click "Start Batch" button
    ↓
Spring Boot POST /api/batch-runs/{id}/start
    ↓
EthernetIPService.sendBatchStart()
    ↓
Write StartCmd=1 to Assembly 150
    ↓
PLC Simulator receives command
Sets BatchStatus=RUNNING
Starts progress simulation
```

## Usage in Templates

### Using Batch Monitor in Other Components
```html
<app-batch-monitor [batchRun]="myBatchRun"></app-batch-monitor>
```

### Custom Polling Configuration
```typescript
// In batch-monitor.component.ts, adjust:
private pollInterval = 2000; // Milliseconds between polls
```

### Progress Status Colors
- **IDLE**: Gray (#6c757d)
- **RUNNING**: Blue (#007bff) with pulsing animation
- **COMPLETED**: Green (#28a745)
- **FAILED**: Red (#dc3545)
- **PENDING**: Yellow (#ffc107)

## Metrics Display

### Progress Bar
- Shows real-time completion percentage
- Color changes based on batch status
- Updates every poll interval (2s)
- Animated width transition

### Quantity Metrics
| Metric | Source | Update Frequency |
|--------|--------|------------------|
| Target Quantity | Assembly 100 | Initial load |
| Current Quantity | Assembly 100 | Every 2 seconds |
| Elapsed Time | Assembly 100 | Every 2 seconds |
| Est. Remaining | Calculated | Every 2 seconds |

## Simulator Behavior Details

### Batch Progression (60 seconds total)
```
Time    Progress    ActualQuantity
0s      0%          0
15s     25%         25% of Target
30s     50%         50% of Target
45s     75%         75% of Target
60s     100%        TargetQuantity (Auto-completes)
```

The simulator provides realistic batch progression:
- ActualQuantity increases proportionally to ProgressPercentage
- Batch automatically completes at 100%
- All data flows through Assembly 100

## Component Hierarchy

```
app-batch-run-list
├── app-batch-monitor (for each batch)
│   ├── Progress Bar (Assembly 100 data)
│   ├── Metrics Grid
│   │   ├── Target Quantity
│   │   ├── Current Quantity
│   │   ├── Elapsed Time
│   │   └── Estimated Remaining
│   └── Simulator Info Panel
├── Action Buttons (triggers Assembly 150)
└── Assembly Legend
```

## Styling Classes

### Status Badges
- `.status-badge.status-pending`
- `.status-badge.status-running`
- `.status-badge.status-completed`
- `.status-badge.status-failed`

### Progress Components
- `.progress-container` - Main progress section
- `.progress-bar-wrapper` - Progress bar background
- `.progress-bar` - Animated progress fill

### Metrics
- `.metrics-grid` - Grid layout for 4 metrics
- `.metric-card` - Individual metric display

## Responsive Design

### Desktop (>1200px)
- Full layout with side-by-side stats
- All metrics visible in 4-column grid
- Action buttons in single row

### Tablet (768px - 1199px)
- Stacked batch summary
- 2-column metrics grid
- Responsive flex buttons

### Mobile (<768px)
- Single column layout
- 1-column metrics grid
- Full-width buttons
- Optimized font sizes

## Error Handling

### Network Errors
- Displays error message with retry option
- Auto-stops polling on repeated failures
- Shows last known values until refresh

### PLC Connection Issues
- Falls back to offline mode
- Still shows progress simulation
- Warning indicator in header

## Future Enhancements

1. **Real Hardware Integration**
   - Replace mock Assembly 100 data with actual PLC readings
   - Add sensor data visualization

2. **Advanced Monitoring**
   - Temperature/pressure sensor graphs
   - Batch alarms and warnings
   - Event timeline

3. **Historical Tracking**
   - Batch execution history
   - Performance analytics
   - Efficiency reports

4. **Mobile Optimization**
   - Touch-friendly controls
   - Simplified progress view
   - Notification alerts

## Testing the Implementation

### Manual Test Scenario
```
1. Navigate to Batch Runs page
2. Create a new batch run (if none exist)
3. Click "Start Batch" button
   - Triggers Assembly 150: StartCmd=1
   - Backend sends command to PLC simulator
4. Observe batch-monitor component
   - Progress bar increases every 2 seconds
   - ActualQuantity increases proportionally
   - ElapsedTime increments
   - EstimatedRemainingTime decreases
5. After ~60 seconds
   - ProgressPercentage reaches 100%
   - BatchStatus changes to COMPLETED
   - Polling stops automatically
6. Verify in H2 console
   - actualQuantity = targetQuantity in database
```

### Browser Console
- Check for polling errors: `console.error()`
- Verify data refresh: log batchProgress updates
- Monitor network requests: XHR to `/api/batch-runs/{id}`

## Component Files

- **Component Logic**: [batch-monitor.component.ts](c:\code\compactLogix\ui\src\app\components\batch-monitor\batch-monitor.component.ts)
- **Template**: [batch-monitor.component.html](c:\code\compactLogix\ui\src\app\components\batch-monitor\batch-monitor.component.html)
- **Styles**: [batch-monitor.component.scss](c:\code\compactLogix\ui\src\app\components\batch-monitor\batch-monitor.component.scss)
- **List Component** (Updated): [batch-run-list.component.ts](c:\code\compactLogix\ui\src\app\components\batch-run-list\batch-run-list.component.ts)

## Integration Checklist

- ✅ batch-monitor component created
- ✅ Polling logic implemented (2-second intervals)
- ✅ Assembly 100 data display
- ✅ Progress percentage calculation
- ✅ Time estimation logic
- ✅ batch-run-list updated with component
- ✅ Responsive styling for all devices
- ✅ Error handling
- ✅ Assembly legend documentation
- ⏳ Add to app.module.ts declarations
- ⏳ Test with running PLC simulator
