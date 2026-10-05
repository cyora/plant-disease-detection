import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ApiService, errorMessage, percent } from '../../core/api.service';
import { PageResponse, ScanSummary } from '../../core/models';

const PAGE_SIZE = 10;

/** List of past analyses, most recent first, with pagination and delete. */
@Component({
  selector: 'app-history-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './history-page.html',
  styleUrl: './history-page.scss',
})
export class HistoryPage {
  readonly api = inject(ApiService);

  readonly data = signal<PageResponse<ScanSummary> | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly deletingId = signal<number | null>(null);

  readonly percent = percent;

  constructor() {
    this.load(0);
  }

  load(page: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.history(page, PAGE_SIZE).subscribe({
      next: (data) => {
        // If the last item of a page was deleted, go back one page
        if (data.content.length === 0 && data.page > 0) {
          this.load(data.page - 1);
          return;
        }
        this.data.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  remove(scan: ScanSummary): void {
    if (!confirm('Delete this analysis and its photos? This cannot be undone.')) {
      return;
    }
    this.deletingId.set(scan.id);
    this.api.deleteScan(scan.id).subscribe({
      next: () => {
        this.deletingId.set(null);
        this.load(this.data()?.page ?? 0);
      },
      error: (err) => {
        this.deletingId.set(null);
        this.error.set(errorMessage(err));
      },
    });
  }

  title(scan: ScanSummary): string {
    if (scan.uncertain) {
      return 'Uncertain result';
    }
    return scan.healthy ? 'No disease detected' : scan.diseaseName;
  }

  status(scan: ScanSummary): 'uncertain' | 'healthy' | 'diseased' {
    if (scan.uncertain) {
      return 'uncertain';
    }
    return scan.healthy ? 'healthy' : 'diseased';
  }
}
