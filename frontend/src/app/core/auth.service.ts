import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router, CanActivateFn } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Models ─────────────────────────────────────────────────────────────────────
export type UserRole = 'ADMIN' | 'USER';
export interface AuthResponse { token: string; username: string; role: UserRole; bearerType?: string; }
export interface Employee { id?: number; username: string; email: string; age: number; mobile: string; firstName: string; lastName: string; department?: string | null; salary?: number | null; createdAt?: string; createdBy?: string; }
export interface PageEmployee { content: Employee[]; totalElements: number; totalPages: number; size: number; number: number; first?: boolean; last?: boolean; }
export const VALIDATION = {
  usernamePattern: '^[a-zA-Z0-9_]{3,50}$',
  mobilePattern: '^[0-9]{10}$'
} as const;

// ── Auth Guard ─────────────────────────────────────────────────────────────────
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isLoggedIn() ? true : router.createUrlTree(['/login']);
};

// ── Auth Interceptor ───────────────────────────────────────────────────────────
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();
  const withAuth = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(withAuth).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/api/auth/')) { auth.logout(); router.navigate(['/login']); }
      return throwError(() => err);
    })
  );
};

// ── Auth Service ───────────────────────────────────────────────────────────────
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api  = environment.apiUrl;

  readonly token    = signal<string | null>(null);
  readonly username = signal<string | null>(null);
  readonly role     = signal<UserRole | null>(null);

  constructor() {
    const t = sessionStorage.getItem('token');
    const u = sessionStorage.getItem('username');
    const r = sessionStorage.getItem('role') as UserRole | null;
    if (t) this.token.set(t);
    if (u) this.username.set(u);
    if (r === 'ADMIN' || r === 'USER') this.role.set(r);
  }

  isLoggedIn(): boolean { return !!this.token(); }
  isAdmin(): boolean    { return this.role() === 'ADMIN'; }

  setSession(res: AuthResponse): void {
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('username', res.username);
    sessionStorage.setItem('role', res.role);
    this.token.set(res.token); this.username.set(res.username); this.role.set(res.role);
  }

  logout(): void {
    ['token', 'username', 'role'].forEach(k => sessionStorage.removeItem(k));
    this.token.set(null); this.username.set(null); this.role.set(null);
  }

  login(body: { username: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/api/auth/login`, body);
  }

  register(body: { username: string; email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/api/auth/register`, body);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.api}/api/auth/change-password`, { currentPassword, newPassword });
  }
}
