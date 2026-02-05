import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RecipeService } from '../../services/recipe.service';
import { Recipe } from '../../models/recipe.model';

@Component({
  selector: 'app-recipe-list',
  templateUrl: './recipe-list.component.html',
  styleUrls: ['./recipe-list.component.scss']
})
export class RecipeListComponent implements OnInit {
  recipes: Recipe[] = [];
  loading = false;
  error: string | null = null;
  displayInactive = false;

  constructor(
    private recipeService: RecipeService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadRecipes();
  }

  loadRecipes(): void {
    this.loading = true;
    this.error = null;

    const request = this.displayInactive ?
      this.recipeService.getAllRecipes() :
      this.recipeService.getActiveRecipes();

    request.subscribe({
      next: (data) => {
        this.recipes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load recipes: ' + (err.message || 'Unknown error');
        this.loading = false;
        console.error(err);
      }
    });
  }

  createRecipe(): void {
    this.router.navigate(['/recipes/new']);
  }

  editRecipe(id: number | undefined): void {
    if (id) {
      this.router.navigate(['/recipes', id, 'edit']);
    }
  }

  deleteRecipe(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this recipe?')) {
      this.recipeService.deleteRecipe(id).subscribe({
        next: () => {
          this.loadRecipes();
        },
        error: (err) => {
          this.error = 'Failed to delete recipe: ' + (err.message || 'Unknown error');
          console.error(err);
        }
      });
    }
  }

  toggleRecipeActive(recipe: Recipe): void {
    if (!recipe.id) return;

    const action = recipe.isActive ?
      this.recipeService.deactivateRecipe(recipe.id) :
      this.recipeService.activateRecipe(recipe.id);

    action.subscribe({
      next: () => {
        this.loadRecipes();
      },
      error: (err) => {
        this.error = 'Failed to update recipe: ' + (err.message || 'Unknown error');
        console.error(err);
      }
    });
  }

  toggleShowInactive(): void {
    this.displayInactive = !this.displayInactive;
    this.loadRecipes();
  }
}
