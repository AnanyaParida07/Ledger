import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { passwordChangeGuard } from './core/guards/password-change.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'analytics', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'change-password',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/change-password/change-password.component').then((m) => m.ChangePasswordComponent),
  },
  {
    path: '',
    canActivate: [authGuard, passwordChangeGuard],
    loadComponent: () => import('./shared/nav-shell/nav-shell.component').then((m) => m.NavShellComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./features/analytics/analytics.component').then((m) => m.AnalyticsComponent),
      },
      {
        path: 'transactions',
        loadComponent: () =>
          import('./features/transactions/transactions.component').then((m) => m.TransactionsComponent),
      },
      {
        path: 'budgets',
        loadComponent: () => import('./features/budgets/budgets.component').then((m) => m.BudgetsComponent),
      },
      {
        path: 'recurring',
        loadComponent: () => import('./features/recurring/recurring.component').then((m) => m.RecurringComponent),
      },
      {
        path: 'team',
        canActivate: [adminGuard],
        loadComponent: () => import('./features/team/team.component').then((m) => m.TeamComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
