/** TypeScript versions of the JSON returned by the Spring Boot API. */

export interface Disease {
  id: number;
  className: string;
  plant: string;
  diseaseName: string;
  healthy: boolean;
  pathogen: string | null;
  description: string | null;
  symptoms: string | null;
  treatment: string | null;
  prevention: string | null;
}

export interface PredictionItem {
  position: number;
  className: string;
  plant: string;
  diseaseName: string;
  healthy: boolean;
  confidence: number;
}

export interface ScanDetail {
  id: number;
  createdAt: string;
  uncertain: boolean;
  confidence: number;
  threshold: number;
  disease: Disease;
  top3: PredictionItem[];
  imageUrl: string;
  heatmapUrl: string | null;
  inferenceMs: number | null;
  gradcamMs: number | null;
}

export interface ScanSummary {
  id: number;
  createdAt: string;
  className: string;
  plant: string;
  diseaseName: string;
  healthy: boolean;
  confidence: number;
  uncertain: boolean;
  imageUrl: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CountItem {
  label: string;
  count: number;
}

export interface DailyCount {
  date: string;
  count: number;
}

export interface Stats {
  totalScans: number;
  confidentScans: number;
  uncertainScans: number;
  healthyScans: number;
  diseasedScans: number;
  averageConfidence: number;
  topDiseases: CountItem[];
  scansByPlant: CountItem[];
  scansLast30Days: DailyCount[];
}
