export interface Recipe {
  id?: number;
  name: string;
  description?: string;
  batchSize: number;
  unit: string;
  isActive: boolean;
  ingredients?: Ingredient[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface Ingredient {
  id?: number;
  name: string;
  quantity: number;
  unit: string;
  notes?: string;
  sequenceOrder: number;
}

export interface BatchRun {
  id?: number;
  recipeId: number;
  batchNumber: string;
  targetQuantity: number;
  actualQuantity?: number;
  status: string;
  startedAt?: Date;
  completedAt?: Date;
  notes?: string;
  operatorName?: string;
}

export interface BatchProgress {
  batchNumber: string;
  targetQuantity: number;
  currentQuantity: number;
  status: string;
}

export interface PLCStatus {
  connected: boolean;
  offlineMode: boolean;
  status: string;
}

export interface RecipeMetrics {
  currentQuantity: number;
  progressPercentage: number;
  elapsedSeconds: number;
  efficiency: number;
}
