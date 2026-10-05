import { Component, OnDestroy, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { ApiService, errorMessage } from '../../core/api.service';

const MAX_BYTES = 5 * 1024 * 1024;
const ACCEPTED_TYPES = ['image/jpeg', 'image/png'];

/** Home page: choose a leaf photo and send it for analysis. */
@Component({
  selector: 'app-analyze-page',
  templateUrl: './analyze-page.html',
  styleUrl: './analyze-page.scss',
})
export class AnalyzePage implements OnDestroy {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  readonly file = signal<File | null>(null);
  readonly preview = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);
  readonly dragging = signal(false);

  onFileInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.select(input.files?.[0]);
    input.value = ''; // allows choosing the same file again
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(true);
  }

  onDragLeave(): void {
    this.dragging.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragging.set(false);
    this.select(event.dataTransfer?.files?.[0]);
  }

  clear(): void {
    this.revokePreview();
    this.file.set(null);
    this.error.set(null);
  }

  analyze(): void {
    const file = this.file();
    if (!file || this.loading()) {
      return;
    }
    this.loading.set(true);
    this.error.set(null);
    this.api.analyze(file).subscribe({
      next: (scan) => this.router.navigate(['/scans', scan.id]),
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  ngOnDestroy(): void {
    this.revokePreview();
  }

  /** Checks the file in the browser before sending it, so the user gets an instant answer. */
  private select(file: File | undefined): void {
    this.error.set(null);
    if (!file) {
      return;
    }
    if (!ACCEPTED_TYPES.includes(file.type)) {
      this.error.set('Choose a JPG or PNG photo.');
      return;
    }
    if (file.size > MAX_BYTES) {
      this.error.set('This photo is larger than 5 MB. Choose a smaller one.');
      return;
    }
    this.revokePreview();
    this.file.set(file);
    this.preview.set(URL.createObjectURL(file));
  }

  private revokePreview(): void {
    const url = this.preview();
    if (url) {
      URL.revokeObjectURL(url);
    }
    this.preview.set(null);
  }
}
