import { Routes } from '@angular/router';

import { AnalyzePage } from './pages/analyze/analyze-page';
import { ResultPage } from './pages/result/result-page';

export const routes: Routes = [
  { path: '', component: AnalyzePage, title: 'Check a leaf | LeafScan' },
  { path: 'scans/:id', component: ResultPage, title: 'Result | LeafScan' },
  { path: '**', redirectTo: '' },
];
