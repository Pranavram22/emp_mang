import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AppUser {
  id: number;
  username: string;
  email: string;
  role: 'ADMIN' | 'USER';
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  listUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.api}/api/admin/users`);
  }

  changeRole(id: number, role: 'ADMIN' | 'USER'): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.api}/api/admin/users/${id}/role`, { role });
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/api/admin/users/${id}`);
  }
}
