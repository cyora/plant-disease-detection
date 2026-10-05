import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_URL } from './api.config';
import { Disease, PageResponse, ScanDetail, ScanSummary, Stats } from './models';

/** All calls to the Spring Boot API go through this service. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  analyze(file: File): Observable<ScanDetail> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ScanDetail>(`${API_URL}/api/scans`, form);
  }

  getScan(id: number): Observable<ScanDetail> {
    return this.http.get<ScanDetail>(`${API_URL}/api/scans/${id}`);
  }

  history(page: number, size: number): Observable<PageResponse<ScanSummary>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<ScanSummary>>(`${API_URL}/api/scans`, { params });
  }

  deleteScan(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/api/scans/${id}`);
  }

  stats(): Observable<Stats> {
    return this.http.get<Stats>(`${API_URL}/api/stats`);
  }

  diseases(plant?: string): Observable<Disease[]> {
    const params = plant ? new HttpParams().set('plant', plant) : undefined;
    return this.http.get<Disease[]>(`${API_URL}/api/diseases`, { params });
  }

  plants(): Observable<string[]> {
    return this.http.get<string[]>(`${API_URL}/api/diseases/plants`);
  }

  /** Turns a path returned by the API (e.g. "/api/scans/1/image") into a full URL. */
  url(path: string): string {
    return `${API_URL}${path}`;
  }
}

/** A readable message for any failed call. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'The server cannot be reached. Check that the backend is running on port 8080.';
    }
    if (error.error?.detail) {
      return error.error.detail;
    }
    return `The request failed (error ${error.status}).`;
  }
  return 'Something went wrong.';
}

/** 0.9931 -> "99.3", 0.0004 -> "<0.1" */
export function percent(value: number): string {
  if (value > 0 && value < 0.001) {
    return '<0.1';
  }
  return (value * 100).toFixed(1);
}
