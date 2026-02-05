import { Component, OnInit } from '@angular/core';
import { RecipeService } from '../../services/recipe.service';
import { Recipe, BatchRun } from '../../models/recipe.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  recipes: Recipe[] = [];
  batchRuns: BatchRun[] = [];
  plcStatus: any = null;
  loading = false;
  error: string | null = null;

  constructor(private recipeService: RecipeService) { }

  ngOnInit(): void {
    this.loadDashboardData();
    // Refresh every 10 seconds
    setInterval(() => this.loadDashboardData(), 10000);
  }

  private loadDashboardData(): void {
    this.loading = true;
    this.error = null;

    this.recipeService.getActiveRecipes().subscribe({
      next: (data) => {
        this.recipes = data;
      },
      error: (err) => {
        this.error = 'Failed to load recipes';
        console.error(err);
      }
    });

    this.recipeService.getAllBatchRuns().subscribe({
      next: (data) => {
        this.batchRuns = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load batch runs';
        this.loading = false;
        console.error(err);
      }
    });

    this.recipeService.getPLCStatus().subscribe({
      next: (data) => {
        this.plcStatus = data;
      },
      error: (err) => {
        console.error('Failed to load PLC status', err);
      }
    });
  }

  getRunningBatches(): BatchRun[] {
    return this.batchRuns.filter(b => b.status === 'RUNNING');
  }

  getPendingBatches(): BatchRun[] {
    return this.batchRuns.filter(b => b.status === 'PENDING');
  }

  getCompletedBatches(): BatchRun[] {
    return this.batchRuns.filter(b => b.status === 'COMPLETED');
  }
}
