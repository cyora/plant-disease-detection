import { Component, computed, inject, signal } from '@angular/core';

import { ApiService, errorMessage } from '../../core/api.service';
import { Disease } from '../../core/models';

/** The 38 classes the model knows, filterable by plant. */
@Component({
  selector: 'app-library-page',
  templateUrl: './library-page.html',
  styleUrl: './library-page.scss',
})
export class LibraryPage {
  private readonly api = inject(ApiService);

  readonly diseases = signal<Disease[]>([]);
  readonly selectedPlant = signal<string | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /** Plant names, taken from the loaded data. */
  readonly plants = computed(() => [...new Set(this.diseases().map((d) => d.plant))].sort());

  /** The list shown, filtered in the browser (all 38 entries are loaded once). */
  readonly visible = computed(() => {
    const plant = this.selectedPlant();
    return plant ? this.diseases().filter((d) => d.plant === plant) : this.diseases();
  });

  constructor() {
    this.api.diseases().subscribe({
      next: (diseases) => {
        this.diseases.set(diseases);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  select(plant: string | null): void {
    this.selectedPlant.set(plant);
  }
}
