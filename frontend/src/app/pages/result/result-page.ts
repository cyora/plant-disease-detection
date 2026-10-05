import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ApiService, errorMessage, percent } from '../../core/api.service';
import { ScanDetail } from '../../core/models';
import { ConfidenceBar } from '../../shared/confidence-bar';

/** Result of one analysis: verdict, confidence, photo and heatmap, top 3, disease information. */
@Component({
  selector: 'app-result-page',
  imports: [RouterLink, DatePipe, LowerCasePipe, ConfidenceBar],
  templateUrl: './result-page.html',
  styleUrl: './result-page.scss',
})
export class ResultPage {
  readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly scan = signal<ScanDetail | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(true);

  readonly percent = percent;

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getScan(id).subscribe({
      next: (scan) => {
        this.scan.set(scan);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }
}
