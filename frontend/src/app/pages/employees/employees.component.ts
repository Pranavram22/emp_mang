import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth.service';
import { EmployeeService } from '../../core/employee.service';
import { StatsService } from '../../core/stats.service';
import { Employee, PageEmployee, VALIDATION } from '../../core/models';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './employees.component.html',
  styleUrl: './employees.component.scss'
})
export class EmployeesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(EmployeeService);
  private readonly statsService = inject(StatsService);

  readonly auth = inject(AuthService);

  readonly pageData = signal<PageEmployee | null>(null);
  readonly loading = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly modalOpen = signal(false);
  readonly editingId = signal<number | null>(null);
  readonly viewEmployee = signal<Employee | null>(null);
  readonly departments = signal<string[]>([]);

  readonly filterForm = this.fb.nonNullable.group({
    q: [''],
    department: ['']
  });

  readonly sortControl = this.fb.nonNullable.control('id,asc');

  readonly pageSize = 10;
  readonly currentPageIndex = signal(0);

  readonly empForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.pattern(VALIDATION.usernamePattern)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    age: [null as number | null, [Validators.required, Validators.min(18), Validators.max(100)]],
    mobile: ['', [Validators.required, Validators.pattern(VALIDATION.mobilePattern)]],
    firstName: ['', [Validators.required, Validators.maxLength(80)]],
    lastName: ['', [Validators.required, Validators.maxLength(80)]],
    department: ['', [Validators.maxLength(80)]],
    salary: [null as number | null]
  });

  ngOnInit(): void {
    this.reload();
    this.statsService.departments().subscribe((d) => this.departments.set(d));
  }

  openView(e: Employee): void {
    this.viewEmployee.set(e);
  }

  closeView(): void {
    this.viewEmployee.set(null);
  }

  onSortChange(): void {
    this.currentPageIndex.set(0);
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    const f = this.filterForm.getRawValue();
    this.api
      .list({
        page: this.currentPageIndex(),
        size: this.pageSize,
        q: f.q || undefined,
        department: f.department || undefined,
        sort: this.sortControl.value
      })
      .subscribe({
        next: (p) => {
          this.pageData.set(p);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
  }

  applyFilters(): void {
    this.currentPageIndex.set(0);
    this.reload();
  }

  setPage(i: number): void {
    this.currentPageIndex.set(i);
    this.reload();
  }

  prev(): void {
    const p = this.pageData();
    if (!p || p.first) return;
    this.currentPageIndex.update((x) => Math.max(0, x - 1));
    this.reload();
  }

  next(): void {
    const p = this.pageData();
    if (!p || p.last) return;
    this.currentPageIndex.update((x) => x + 1);
    this.reload();
  }

  openCreate(): void {
    this.saveError.set(null);
    this.editingId.set(null);
    this.empForm.reset({
      username: '',
      email: '',
      age: null,
      mobile: '',
      firstName: '',
      lastName: '',
      department: '',
      salary: null
    });
    this.modalOpen.set(true);
  }

  openEdit(e: Employee): void {
    this.saveError.set(null);
    this.editingId.set(e.id ?? null);
    this.empForm.patchValue({
      username: e.username,
      email: e.email,
      age: e.age,
      mobile: e.mobile,
      firstName: e.firstName,
      lastName: e.lastName,
      department: e.department ?? '',
      salary:
        e.salary !== undefined && e.salary !== null && !Number.isNaN(Number(e.salary))
          ? Number(e.salary)
          : null
    });
    this.modalOpen.set(true);
  }

  closeModal(): void {
    this.modalOpen.set(false);
  }

  backdropClick(ev: MouseEvent): void {
    if (ev.target === ev.currentTarget) this.closeModal();
  }

  save(): void {
    this.saveError.set(null);
    if (this.empForm.invalid) {
      this.empForm.markAllAsTouched();
      return;
    }
    const raw = this.empForm.getRawValue();
    const salaryNum = raw.salary;
    const body: Employee = {
      username: raw.username,
      email: raw.email,
      age: Number(raw.age),
      mobile: raw.mobile,
      firstName: raw.firstName,
      lastName: raw.lastName,
      department: raw.department?.trim() ? raw.department.trim() : undefined,
      salary:
        salaryNum !== null && salaryNum !== undefined && !Number.isNaN(Number(salaryNum))
          ? Number(salaryNum)
          : undefined
    };
    const id = this.editingId();
    const req = id == null ? this.api.create(body) : this.api.update(id, body);
    req.subscribe({
      next: () => {
        this.closeModal();
        this.reload();
      },
      error: (err) => this.saveError.set(this.formatErr(err?.error))
    });
  }

  deleteRow(e: Employee): void {
    if (!e.id) return;
    if (!window.confirm(`Delete employee ${e.username}?`)) return;
    this.api.delete(e.id).subscribe({
      next: () => this.reload(),
      error: (err) => window.alert(this.formatErr(err?.error))
    });
  }

  exportPdf(): void {
    const f = this.filterForm.getRawValue();
    this.api.exportPdf(f.q || undefined, f.department || undefined).subscribe({
      next: (blob) => this.downloadBlob(blob, 'employees.pdf'),
      error: () => window.alert('PDF export failed (admin only).')
    });
  }

  exportExcel(): void {
    const f = this.filterForm.getRawValue();
    this.api.exportExcel(f.q || undefined, f.department || undefined).subscribe({
      next: (blob) => this.downloadBlob(blob, 'employees.xlsx'),
      error: () => window.alert('Excel export failed (admin only).')
    });
  }

  /** Page numbers centered around current (0-based). */
  pageNumbers(): number[] {
    const p = this.pageData();
    if (!p) return [];
    const total = p.totalPages;
    const cur = p.number;
    const window = 5;
    const start = Math.max(0, Math.min(cur - 2, total - window));
    const end = Math.min(total, start + window);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  private formatErr(payload: unknown): string {
    if (!payload || typeof payload !== 'object') return 'Request failed';
    const p = payload as Record<string, unknown>;
    const msg = p['message'];
    if (typeof msg === 'string') return msg;
    const errs = p['errors'];
    if (errs && typeof errs === 'object') {
      const e = errs as Record<string, string>;
      const v = Object.values(e)[0];
      return typeof v === 'string' ? v : 'Validation failed';
    }
    return 'Request failed';
  }
}
