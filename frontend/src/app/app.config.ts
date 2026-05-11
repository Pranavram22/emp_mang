import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor, authGuard } from './core/auth.service';

export const routes: Routes = [
  { path: 'login',       loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'register',    loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard',   loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),  canActivate: [authGuard] },
  { path: 'employees',   loadComponent: () => import('./pages/employees/employees.component').then(m => m.EmployeesComponent),  canActivate: [authGuard] },
  { path: 'admin/users', loadComponent: () => import('./pages/admin-users/admin-users.component').then(m => m.AdminUsersComponent), canActivate: [authGuard] },
  { path: '',  pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
