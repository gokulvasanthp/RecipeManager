import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Recipe, BatchRun, BatchProgress, PLCStatus } from '../models/recipe.model';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) { }

  // Recipe endpoints
  createRecipe(recipe: Recipe): Observable<Recipe> {
    return this.http.post<Recipe>(`${this.apiUrl}/recipes`, recipe);
  }

  getRecipe(id: number): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.apiUrl}/recipes/${id}`);
  }

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/recipes`);
  }

  getActiveRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/recipes/active`);
  }

  updateRecipe(id: number, recipe: Recipe): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.apiUrl}/recipes/${id}`, recipe);
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/recipes/${id}`);
  }

  activateRecipe(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/recipes/${id}/activate`, {});
  }

  deactivateRecipe(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/recipes/${id}/deactivate`, {});
  }

  // Batch run endpoints
  createBatchRun(batchRun: BatchRun): Observable<BatchRun> {
    return this.http.post<BatchRun>(`${this.apiUrl}/batch-runs`, batchRun);
  }

  getBatchRun(id: number): Observable<BatchRun> {
    return this.http.get<BatchRun>(`${this.apiUrl}/batch-runs/${id}`);
  }

  getAllBatchRuns(): Observable<BatchRun[]> {
    return this.http.get<BatchRun[]>(`${this.apiUrl}/batch-runs`);
  }

  getBatchRunsByRecipe(recipeId: number): Observable<BatchRun[]> {
    return this.http.get<BatchRun[]>(`${this.apiUrl}/batch-runs/recipe/${recipeId}`);
  }

  updateBatchRunStatus(id: number, status: string): Observable<BatchRun> {
    return this.http.put<BatchRun>(`${this.apiUrl}/batch-runs/${id}/status`, { status });
  }

  completeBatchRun(id: number, actualQuantity: number): Observable<BatchRun> {
    return this.http.put<BatchRun>(`${this.apiUrl}/batch-runs/${id}/complete`, { actualQuantity });
  }

  deleteBatchRun(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/batch-runs/${id}`);
  }

  startBatch(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/batch-runs/${id}/start`, {});
  }

  stopBatch(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/batch-runs/${id}/stop`, {});
  }

  getBatchProgress(id: number): Observable<BatchProgress> {
    return this.http.get<BatchProgress>(`${this.apiUrl}/batch-runs/${id}/progress`);
  }

  getRecipeMetrics(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/batch-runs/${id}/recipe-metrics`);
  }

  // PLC endpoints
  getPLCStatus(): Observable<PLCStatus> {
    return this.http.get<PLCStatus>(`${this.apiUrl}/batch-runs/plc-status`);
  }

  setPLCMode(offlineMode: boolean): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/batch-runs/plc-mode`, { offlineMode });
  }

  enableOfflineMode(): Observable<any> {
    return this.setPLCMode(true);
  }

  enableOnlineMode(): Observable<any> {
    return this.setPLCMode(false);
  }
}
