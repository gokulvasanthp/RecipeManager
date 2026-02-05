import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { RecipeListComponent } from './components/recipe-list/recipe-list.component';
import { RecipeFormComponent } from './components/recipe-form/recipe-form.component';
import { BatchRunListComponent } from './components/batch-run-list/batch-run-list.component';

const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'recipes', component: RecipeListComponent },
  { path: 'recipes/new', component: RecipeFormComponent },
  { path: 'recipes/:id/edit', component: RecipeFormComponent },
  { path: 'batch-runs', component: BatchRunListComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
