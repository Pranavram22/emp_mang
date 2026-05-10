import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, UserRole } from './models';

export interface LoginBody {
  username: string;
  password: string;
}

export interface RegisterBody {
  username: string;
  email: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  readonly token = signal<string | null>(null);
  readonly username = signal<string | null>(null);
  readonly role = signal<UserRole | null>(null);

  constructor() {
    if (typeof sessionStorage !== 'undefined') {
      const t = sessionStorage.getItem('token');
      const u = sessionStorage.getItem('username');
      const r = sessionStorage.getItem('role') as UserRole | null;
      if (t) this.token.set(t);
      if (u) this.username.set(u);
      if (r === 'ADMIN' || r === 'USER') this.role.set(r);
    }
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  isAdmin(): boolean {
    return this.role() === 'ADMIN';
  }

  setSession(res: AuthResponse): void {
    sessionStorage.setItem('token', res.token);
    sessionStorage.setItem('username', res.username);
    sessionStorage.setItem('role', res.role);
    this.token.set(res.token);
    this.username.set(res.username);
    this.role.set(res.role);
  }

  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('username');
    sessionStorage.removeItem('role');
    this.token.set(null);
    this.username.set(null);
    this.role.set(null);
  }

  login(body: LoginBody) {
    return this.http.post<AuthResponse>(`${this.api}/api/auth/login`, body);
  }

  register(body: RegisterBody) {
    return this.http.post<AuthResponse>(`${this.api}/api/auth/register`, body);
  }

  changePassword(currentPassword: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.api}/api/auth/change-password`, {
      currentPassword,
      newPassword
    });
  }
}
