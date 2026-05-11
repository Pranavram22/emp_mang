import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

// ── Stats ──────────────────────────────────────────────────────────────────────
export interface Stats { totalEmployees: number; totalDepartments: number; avgSalary: number; addedThisMonth: number; highestPaidName: string; highestSalary: number; }
export interface DeptCount { department: string; count: number; }

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/api/stats';

  get(): Observable<Stats>           { return this.http.get<Stats>(`${this.base}`); }
  departments(): Observable<string[]>    { return this.http.get<string[]>(`${this.base}/departments`); }
  deptBreakdown(): Observable<DeptCount[]> { return this.http.get<DeptCount[]>(`${this.base}/dept-breakdown`); }
}

// ── Admin ──────────────────────────────────────────────────────────────────────
export interface AppUser { id: number; username: string; email: string; role: 'ADMIN' | 'USER'; }

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl + '/api/admin/users';

  listUsers(): Observable<AppUser[]>                          { return this.http.get<AppUser[]>(this.base); }
  changeRole(id: number, role: 'ADMIN' | 'USER'): Observable<AppUser> { return this.http.put<AppUser>(`${this.base}/${id}/role`, { role }); }
  deleteUser(id: number): Observable<void>                    { return this.http.delete<void>(`${this.base}/${id}`); }
}
