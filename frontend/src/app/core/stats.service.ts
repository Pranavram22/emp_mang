import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Stats {
  totalEmployees: number;
  totalDepartments: number;
  avgSalary: number;
  addedThisMonth: number;
  highestPaidName: string;
  highestSalary: number;
}

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  get(): Observable<Stats> {
    return this.http.get<Stats>(`${this.api}/api/stats`);
  }

  departments(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/api/stats/departments`);
  }
}
