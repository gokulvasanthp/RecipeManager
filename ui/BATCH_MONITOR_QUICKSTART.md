# Batch Monitor Quick Start

## What's New

A **Batch Monitor** component has been added to the batch runs page. It displays real-time progress data from the PLC simulator (Assembly 100).

## Visual Layout

The batch runs page now shows batches as expandable cards:

```
┌─────────────────────────────────────────────┐
│ Batch #001  [RUNNING]  Recipe ID: 5         │ ↻
├─────────────────────────────────────────────┤
│ Target: 100  Actual: 50  Operator: John    │
├─────────────────────────────────────────────┤
│                                            │
│  Progress: 50%                    │Refresh│
│  [████████████░░░░░░░░░░░░░░░░░░]│       │
│                                            │
│  Target: 100    Current: 50    Elapsed: 30s│
│  Est. Time: 30s                            │
│                                            │
│  PLC Simulator Data (Assembly 100):        │
│  • RecipeID: 5                             │
│  • BatchID: 1                              │
│  • Status: RUNNING                         │
│  • ProgressPercentage: 50%                 │
│  • ActualQuantity: 50                      │
│  • ElapsedTime: 30s                        │
│                                            │
├─────────────────────────────────────────────┤
│ [▶ Start] [⏹ Stop] [🗑 Delete]           │
└─────────────────────────────────────────────┘
```

## Key Features

### Real-Time Progress Monitoring
- Progress bar updates every 2 seconds
- Shows percentage complete
- Color-coded status indicators

### Live Metrics
- **Target Quantity**: Expected final amount
- **Current Quantity**: Actual amount (increases with progress)
- **Elapsed Time**: How long the batch has been running
- **Est. Remaining**: Estimated time until completion

### PLC Simulator Data
- Shows all values read from Assembly 100
- Labels indicate data source from PLC
- Transparent view of simulator behavior

### Smart Polling
- Automatically polls data when batch is RUNNING
- Stops polling when batch completes
- Manual refresh button for on-demand updates
- Error messages with retry options

## How to Use

### Start a Batch
1. Go to **Batch Runs** page
2. Create a batch run (if none exist)
3. Click **▶ Start Batch** button
   - This sends StartCmd=1 to Assembly 150
   - PLC simulator receives command

### Monitor Progress
1. Watch the progress bar increase every 2 seconds
2. See actual quantity accumulate
3. Check elapsed time
4. Monitor estimated remaining time

### Stop a Batch
1. Click **⏹ Stop Batch** to halt progress
   - This sends StopCmd=1 to Assembly 150

### Delete a Batch
1. Click **🗑 Delete** to remove from system
2. Confirm deletion when prompted

### Manual Refresh
1. Click **↻** button in top-right of batch card
2. Fetches latest progress data immediately
3. Resumes automatic polling after refresh

## Progress Simulation

The PLC simulator progresses batches over **60 seconds**:

| Time | Progress | Current Qty | Status |
|------|----------|-------------|--------|
| 0s | 0% | 0 | PENDING |
| 15s | 25% | 25 | RUNNING |
| 30s | 50% | 50 | RUNNING |
| 45s | 75% | 75 | RUNNING |
| 60s | 100% | 100 | COMPLETED |

**Note**: Current quantity = (Target quantity × Progress%)

## Status Indicators

- 🔵 **PENDING** (Yellow) - Batch created, not started
- 🔵 **RUNNING** (Blue, pulsing) - Batch in progress
- 🟢 **COMPLETED** (Green) - Batch finished successfully
- 🔴 **FAILED** (Red) - Batch encountered error

## Assembly Information

### Assembly 100 - PLC Sends Data
These values update every 2 seconds while batch is RUNNING:
- RecipeID, BatchID, BatchStatus
- TargetQuantity, ActualQuantity
- ProgressPercentage, ElapsedTime

### Assembly 150 - App Sends Commands
These trigger when buttons are clicked:
- StartCmd (1 = start, 0 = idle)
- StopCmd (1 = stop, 0 = idle)

## EtherNet/IP Communication

```
PLC Simulator (Docker) ←→ Spring Boot Backend ←→ Angular UI
        Port 44818              REST API              Browser
   Assembly 100 ↔ Assembly 150
   (Poll every 2s)
```

## Responsive Design

### Desktop (Wide Screen)
- 4 metric cards in a row
- Full-width progress bar
- All information visible at once

### Tablet
- 2 metric cards per row
- Responsive button layout
- Optimized spacing

### Mobile
- 1 metric card per row
- Full-width buttons
- Touch-friendly controls
- Readable fonts

## Troubleshooting

### Progress Not Updating
- Check if PLC simulator is running
- Verify network connection to `localhost:44818`
- Click refresh button to force update
- Check browser console for errors

### Batch Won't Start
- Ensure batch status is PENDING
- Check Spring Boot backend is running
- Verify PLC endpoint is accessible
- Look for error message in UI

### Slow Updates
- Default polling interval is 2 seconds
- Network latency may cause delays
- Refresh button for immediate update

## Performance Notes

- Polling interval: 2 seconds (configurable)
- Light network usage (~2KB per poll)
- Minimal CPU impact (single subscription)
- Smooth UI animations using CSS

## Future Enhancements

Planned improvements:
- Real hardware PLC connection
- Sensor data visualization (temperature, pressure)
- Batch history and analytics
- PDF batch reports
- Mobile push notifications

## Getting Help

1. **See Data Flow Diagram**: Check `BATCH_MONITOR_GUIDE.md`
2. **Implementation Details**: Read `BATCH_MONITOR_GUIDE.md`
3. **PLC Integration**: Review `PLC_SIMULATOR_SYNC.md`
4. **Architecture**: See `c:\code\dockerConfig\opener-plc\PLC_SIMULATOR_SYNC.md`

## Files Changed

**New:**
- `ui/src/app/components/batch-monitor/` (component)
- `ui/BATCH_MONITOR_GUIDE.md` (detailed docs)

**Updated:**
- `batch-run-list.component.html` (card layout)
- `batch-run-list.component.scss` (new styling)

**For Reference:**
- `BATCH_MONITOR_IMPLEMENTATION_CHECKLIST.md`
- `UI_BATCH_MONITOR_ADDED.md`

---

**Ready to use!** Start the application and navigate to Batch Runs to see the new interface. 🎉
