import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { RecipeService } from '../../services/recipe.service';
import { BatchRun, BatchProgress } from '../../models/recipe.model';
import { interval, Subscription } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

/**
 * Batch Monitor Component
 * Real-time display of batch progress from PLC simulator
 * Displays Assembly 100 data: RecipeID, BatchID, Status, Quantity, Progress
 */
@Component({
  selector: 'app-batch-monitor',
  templateUrl: './batch-monitor.component.html',
  styleUrls: ['./batch-monitor.component.scss']
})
export class BatchMonitorComponent implements OnInit, OnDestroy {
  @Input() batchRun: BatchRun | null = null;

  batchProgress: BatchProgress | null = null;
  loading = false;
  error: string | null = null;
  
  // Simulator-specific fields (Assembly 100 data)
  progressPercentage = 0;
  elapsedTime = 0;
  estimatedRemainingTime = 0;
  simulatorMode = 'OFFLINE';
  
  // Polling subscription
  private pollSubscription: Subscription | null = null;
  private pollInterval = 2000; // 2 seconds (matches batch simulation speed)

  constructor(private recipeService: RecipeService) {}

  ngOnInit(): void {
    if (this.batchRun?.id && this.batchRun.status === 'RUNNING') {
      this.startPolling();
    }
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  /**
   * Start polling batch progress from simulator
   * Assembly 100: reads RecipeID, BatchID, Status, ActualQuantity, ProgressPercentage, ElapsedTime
   */
  startPolling(): void {
    if (!this.batchRun?.id || this.pollSubscription) {
      return;
    }

    this.pollSubscription = interval(this.pollInterval)
      .pipe(
        switchMap(() => {
          if (this.batchRun?.id) {
            return this.recipeService.getBatchProgress(this.batchRun.id);
          }
          return of(null);
        }),
        catchError((err) => {
          console.error('Error polling batch progress:', err);
          this.error = 'Failed to fetch batch progress';
          return of(null);
        })
      )
      .subscribe((progress) => {
        if (progress) {
          this.batchProgress = progress;
          this.updateSimulatorMetrics(progress);

          // Auto-stop polling when batch completes
          if (this.batchRun && progress.status === 'COMPLETED') {
            this.stopPolling();
          }
        }
      });
  }

  /**
   * Update simulator-specific metrics from Assembly 100
   */
  private updateSimulatorMetrics(progress: BatchProgress): void {
    // Calculate progress percentage
    if (progress.targetQuantity > 0) {
      this.progressPercentage = Math.round(
        (progress.currentQuantity / progress.targetQuantity) * 100
      );
    }

    // Estimate remaining time based on progress
    // Assuming 60-second total batch time (matches simulator)
    const totalBatchTime = 60000; // 60 seconds in milliseconds
    const elapsedMs = (this.elapsedTime * 1000) + this.pollInterval;
    this.elapsedTime = Math.floor(elapsedMs / 1000);

    if (this.progressPercentage > 0) {
      const totalTime = (elapsedMs / this.progressPercentage) * 100;
      this.estimatedRemainingTime = Math.max(0, Math.ceil((totalTime - elapsedMs) / 1000));
    }
  }

  stopPolling(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = null;
    }
  }

  /**
   * Refresh progress manually
   */
  refreshProgress(): void {
    if (!this.batchRun?.id) return;

    this.loading = true;
    this.error = null;

    this.recipeService.getBatchProgress(this.batchRun.id).subscribe({
      next: (progress) => {
        this.batchProgress = progress;
        this.updateSimulatorMetrics(progress);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to fetch batch progress: ' + (err.message || 'Unknown error');
        this.loading = false;
      }
    });
  }

  /**
   * Get progress bar color based on status
   */
  getProgressColor(): string {
    if (!this.batchProgress) return '#ccc';

    switch (this.batchProgress.status) {
      case 'RUNNING':
        return '#007bff'; // Blue
      case 'COMPLETED':
        return '#28a745'; // Green
      case 'FAILED':
        return '#dc3545'; // Red
      case 'PENDING':
        return '#ffc107'; // Yellow
      default:
        return '#6c757d'; // Gray
    }
  }

  /**
   * Format seconds to MM:SS
   */
  formatTime(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }

  /**
   * Get status badge class
   */
  getStatusBadgeClass(): string {
    if (!this.batchProgress) return 'badge-secondary';

    switch (this.batchProgress.status) {
      case 'RUNNING':
        return 'badge-primary';
      case 'COMPLETED':
        return 'badge-success';
      case 'FAILED':
        return 'badge-danger';
      case 'PENDING':
        return 'badge-warning';
      default:
        return 'badge-secondary';
    }
  }
}
