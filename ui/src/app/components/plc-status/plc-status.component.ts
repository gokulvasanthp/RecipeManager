import { Component, Input } from '@angular/core';
import { RecipeService } from '../../services/recipe.service';

@Component({
  selector: 'app-plc-status',
  templateUrl: './plc-status.component.html',
  styleUrls: ['./plc-status.component.scss']
})
export class PLCStatusComponent {
  @Input() status: any = null;
  loading = false;
  message = '';

  constructor(private recipeService: RecipeService) { }

  toggleMode(): void {
    this.loading = true;

    if (this.status?.offlineMode) {
      this.recipeService.enableOnlineMode().subscribe({
        next: () => {
          this.status.offlineMode = false;
          this.message = 'Switched to online mode';
          this.loading = false;
          setTimeout(() => this.message = '', 3000);
        },
        error: (err) => {
          this.message = 'Failed to switch to online mode';
          this.loading = false;
          console.error(err);
        }
      });
    } else {
      this.recipeService.enableOfflineMode().subscribe({
        next: () => {
          this.status.offlineMode = true;
          this.message = 'Switched to offline mode';
          this.loading = false;
          setTimeout(() => this.message = '', 3000);
        },
        error: (err) => {
          this.message = 'Failed to switch to offline mode';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }
}
