import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { RecipeService } from '../../services/recipe.service';
import { Recipe } from '../../models/recipe.model';

@Component({
  selector: 'app-recipe-form',
  templateUrl: './recipe-form.component.html',
  styleUrls: ['./recipe-form.component.scss']
})
export class RecipeFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  error: string | null = null;
  recipeId: number | null = null;
  isEditing = false;

  constructor(
    private fb: FormBuilder,
    private recipeService: RecipeService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.recipeId = parseInt(params['id']);
        this.isEditing = true;
        this.loadRecipe(this.recipeId);
      }
    });
  }

  private initializeForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: [''],
      batchSize: ['', [Validators.required, Validators.pattern(/^\d+(\.\d+)?$/)]],
      unit: ['kg', Validators.required],
      isActive: [true],
      ingredients: this.fb.array([])
    });
  }

  private loadRecipe(id: number): void {
    this.loading = true;
    this.recipeService.getRecipe(id).subscribe({
      next: (recipe) => {
        this.populateForm(recipe);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load recipe: ' + (err.message || 'Unknown error');
        this.loading = false;
        console.error(err);
      }
    });
  }

  private populateForm(recipe: Recipe): void {
    this.form.patchValue({
      name: recipe.name,
      description: recipe.description,
      batchSize: recipe.batchSize,
      unit: recipe.unit,
      isActive: recipe.isActive
    });

    const ingredientsArray = this.form.get('ingredients') as FormArray;
    if (recipe.ingredients) {
      recipe.ingredients.forEach(ing => {
        ingredientsArray.push(this.createIngredientGroup(ing));
      });
    }
  }

  private createIngredientGroup(ingredient?: any): FormGroup {
    return this.fb.group({
      id: [ingredient?.id],
      name: [ingredient?.name || '', [Validators.required, Validators.minLength(2)]],
      quantity: [ingredient?.quantity || '', [Validators.required, Validators.pattern(/^\d+(\.\d+)?$/)]],
      unit: [ingredient?.unit || 'kg', Validators.required],
      notes: [ingredient?.notes || ''],
      sequenceOrder: [ingredient?.sequenceOrder || this.getIngredientsArray().length + 1, Validators.required]
    });
  }

  get ingredients(): FormArray {
    return this.form.get('ingredients') as FormArray;
  }

  getIngredientsArray(): any[] {
    return this.ingredients.controls;
  }

  addIngredient(): void {
    this.ingredients.push(this.createIngredientGroup());
  }

  removeIngredient(index: number): void {
    this.ingredients.removeAt(index);
  }

  onSubmit(): void {
    if (!this.form.valid) {
      this.error = 'Please fill all required fields';
      return;
    }

    this.loading = true;
    this.error = null;

    const recipe = this.form.value as Recipe;

    const request = this.isEditing && this.recipeId ?
      this.recipeService.updateRecipe(this.recipeId, recipe) :
      this.recipeService.createRecipe(recipe);

    request.subscribe({
      next: () => {
        this.router.navigate(['/recipes']);
      },
      error: (err) => {
        this.error = 'Failed to save recipe: ' + (err.message || 'Unknown error');
        this.loading = false;
        console.error(err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/recipes']);
  }
}
