import { Component, OnInit } from '@angular/core';
import { RecipeService } from '../../services/recipe.service';
import { BatchRun } from '../../models/recipe.model';

@Component({
  selector: 'app-batch-run-list',
  templateUrl: './batch-run-list.component.html',
  styleUrls: ['./batch-run-list.component.scss']
})
export class BatchRunListComponent implements OnInit {
  batchRuns: BatchRun[] = [];
  loading = false;
  error: string | null = null;

  constructor(private recipeService: RecipeService) { }

  ngOnInit(): void {
    this.loadBatchRuns();
  }

  loadBatchRuns(): void {
    this.loading = true;
    this.error = null;

    this.recipeService.getAllBatchRuns().subscribe({
      next: (data) => {
        this.batchRuns = data.sort((a, b) => {
          const dateA = new Date(a.startedAt || 0).getTime();
          const dateB = new Date(b.startedAt || 0).getTime();
          return dateB - dateA;
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load batch runs: ' + (err.message || 'Unknown error');
        this.loading = false;
        console.error(err);
      }
    });
  }

  getStatusClass(status: string): string {
    return 'status-' + status.toLowerCase();
  }

  startBatch(id: number | undefined): void {
    if (!id) return;

    this.recipeService.startBatch(id).subscribe({
      next: () => {
        this.loadBatchRuns();
      },
      error: (err) => {
        this.error = 'Failed to start batch: ' + (err.message || 'Unknown error');
        console.error(err);
      }
    });
  }

  stopBatch(id: number | undefined): void {
    if (!id) return;

    this.recipeService.stopBatch(id).subscribe({
      next: () => {
        this.loadBatchRuns();
      },
      error: (err) => {
        this.error = 'Failed to stop batch: ' + (err.message || 'Unknown error');
        console.error(err);
      }
    });
  }

  deleteBatchRun(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this batch run?')) {
      this.recipeService.deleteBatchRun(id).subscribe({
        next: () => {
          this.loadBatchRuns();
        },
        error: (err) => {
          this.error = 'Failed to delete batch run: ' + (err.message || 'Unknown error');
          console.error(err);
        }
      });
    }
  }
}
