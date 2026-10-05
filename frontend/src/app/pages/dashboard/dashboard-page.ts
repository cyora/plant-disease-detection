import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ApiService, errorMessage, percent } from '../../core/api.service';
import { CountItem, Stats } from '../../core/models';

/** Statistics on all analyses. Charts are plain HTML and CSS, no chart library. */
@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly api = inject(ApiService);

  readonly stats = signal<Stats | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly percent = percent;

  /** Share of each outcome, for the stacked bar. */
  readonly outcome = computed(() => {
    const s = this.stats();
    if (!s || s.totalScans === 0) {
      return null;
    }
    const share = (n: number) => (n / s.totalScans) * 100;
    return {
      diseased: share(s.diseasedScans),
      healthy: share(s.healthyScans),
      uncertain: share(s.uncertainScans),
    };
  });

  /** Highest daily count, to scale the activity columns. */
  readonly maxDaily = computed(() =>
    Math.max(1, ...(this.stats()?.scansLast30Days ?? []).map((d) => d.count)),
  );

  constructor() {
    this.api.stats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  /** Width of a bar relative to the largest item of its list. */
  barWidth(item: CountItem, items: CountItem[]): number {
    const max = Math.max(...items.map((i) => i.count));
    return max === 0 ? 0 : (item.count / max) * 100;
  }
}
