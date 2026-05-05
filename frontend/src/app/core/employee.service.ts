import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Employee, PageEmployee } from './models';

export interface EmployeeListParams {
  page: number;
  size: number;
  q?: string;
  department?: string;
  sort?: string;
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

  exportPdf(q?: string, department?: string): Observable<Blob> {
    let params = new HttpParams();
    if (q?.trim()) params = params.set('q', q.trim());
    if (department?.trim()) params = params.set('department', department.trim());
    return this.http.get(`${this.api}/api/employees/export/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  exportExcel(q?: string, department?: string): Observable<Blob> {
    let params = new HttpParams();
    if (q?.trim()) params = params.set('q', q.trim());
    if (department?.trim()) params = params.set('department', department.trim());
    return this.http.get(`${this.api}/api/employees/export/excel`, {
      params,
      responseType: 'blob'
    });
  }
}
