import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, PageEmployee } from './auth.service';

export interface EmployeeListParams {
  page: number;
  size: number;
  q?: string;
  department?: string;
  sort?: string;
  minSalary?: number | null;
  maxSalary?: number | null;
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  list(p: EmployeeListParams): Observable<PageEmployee> {
    let params = new HttpParams()
      .set('page', String(p.page))
      .set('size', String(p.size))
      .set('sort', p.sort ?? 'id,asc');
    if (p.q?.trim()) params = params.set('q', p.q.trim());
    if (p.department?.trim()) params = params.set('department', p.department.trim());
    if (p.minSalary != null) params = params.set('minSalary', String(p.minSalary));
    if (p.maxSalary != null) params = params.set('maxSalary', String(p.maxSalary));
    return this.http.get<PageEmployee>(`${this.api}/api/employees`, { params });
  }

  getOne(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.api}/api/employees/${id}`);
  }

  create(body: Employee): Observable<Employee> {
    return this.http.post<Employee>(`${this.api}/api/employees`, body);
  }

  update(id: number, body: Employee): Observable<Employee> {
    return this.http.put<Employee>(`${this.api}/api/employees/${id}`, body);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/api/employees/${id}`);
  }

  exportPdf(q?: string, department?: string, minSalary?: number | null, maxSalary?: number | null): Observable<Blob> {
    let params = new HttpParams();
    if (q?.trim()) params = params.set('q', q.trim());
    if (department?.trim()) params = params.set('department', department.trim());
    if (minSalary != null) params = params.set('minSalary', String(minSalary));
    if (maxSalary != null) params = params.set('maxSalary', String(maxSalary));
    return this.http.get(`${this.api}/api/employees/export/pdf`, { params, responseType: 'blob' });
  }

  downloadImportTemplate(): Observable<Blob> {
    return this.http.get(`${this.api}/api/employees/import/template`, { responseType: 'blob' });
  }

  importExcel(file: File): Observable<{ imported: number; skipped: number; errors: string[] }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imported: number; skipped: number; errors: string[] }>(
      `${this.api}/api/employees/import/excel`, form
    );
  }

  exportExcel(q?: string, department?: string, minSalary?: number | null, maxSalary?: number | null): Observable<Blob> {
    let params = new HttpParams();
    if (q?.trim()) params = params.set('q', q.trim());
    if (department?.trim()) params = params.set('department', department.trim());
    if (minSalary != null) params = params.set('minSalary', String(minSalary));
    if (maxSalary != null) params = params.set('maxSalary', String(maxSalary));
    return this.http.get(`${this.api}/api/employees/export/excel`, { params, responseType: 'blob' });
  }
}
