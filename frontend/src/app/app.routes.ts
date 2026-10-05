import { Routes } from '@angular/router';

import { AnalyzePage } from './pages/analyze/analyze-page';
import { DashboardPage } from './pages/dashboard/dashboard-page';
import { HistoryPage } from './pages/history/history-page';
import { LibraryPage } from './pages/library/library-page';
import { ResultPage } from './pages/result/result-page';

export const routes: Routes = [
  { path: '', component: AnalyzePage, title: 'Check a leaf | LeafScan' },
  { path: 'scans/:id', component: ResultPage, title: 'Result | LeafScan' },
  { path: 'history', component: HistoryPage, title: 'History | LeafScan' },
  { path: 'dashboard', component: DashboardPage, title: 'Dashboard | LeafScan' },
  { path: 'diseases', component: LibraryPage, title: 'Diseases | LeafScan' },
  { path: '**', redirectTo: '' },
];
