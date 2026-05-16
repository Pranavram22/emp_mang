import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService, Employee, PageEmployee, VALIDATION } from '../../core/auth.service';
import { EmployeeService } from '../../core/employee.service';
import { StatsService } from '../../core/stats.service';
import { ToastService } from '../../core/toast.service';

interface ModalState { open: boolean; editingId: number | null; error: string | null; }

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './employees.component.html'
})
export class EmployeesComponent implements OnInit {
  private readonly fb      = inject(FormBuilder);
  private readonly api     = inject(EmployeeService);
  private readonly stats   = inject(StatsService);
  private readonly toast   = inject(ToastService);
  readonly auth            = inject(AuthService);

  // Page state
  readonly pageData    = signal<PageEmployee | null>(null);
  readonly loading     = signal(false);
  readonly departments = signal<string[]>([]);
  readonly pageSize    = signal(10);
  readonly pageIndex   = signal(0);
  readonly pageSizeOptions = [5, 10, 25, 50];

  // Modal state
  readonly modal = signal<ModalState>({ open: false, editingId: null, error: null });
  readonly view  = signal<Employee | null>(null);

  readonly filterForm = this.fb.nonNullable.group({
    q:          [''],
    department: [''],
    minSalary:  [null as number | null],
    maxSalary:  [null as number | null]
  });

  readonly sortControl = this.fb.nonNullable.control('id,asc');

  readonly empForm = this.fb.nonNullable.group({
    username:   ['', [Validators.required, Validators.pattern(VALIDATION.usernamePattern)]],
    email:      ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    age:        [null as number | null, [Validators.required, Validators.min(18), Validators.max(100)]],
    mobile:     ['', [Validators.required, Validators.pattern(VALIDATION.mobilePattern)]],
    firstName:  ['', [Validators.required, Validators.maxLength(80)]],
    lastName:   ['', [Validators.required, Validators.maxLength(80)]],
    department: ['', [Validators.maxLength(80)]],
    salary:     [null as number | null]
  });

  ngOnInit(): void {
    this.reload();
    this.stats.departments().subscribe(d => this.departments.set(d));
  }

  // ── Reload ──────────────────────────────────────────────────────────────────

  reload(): void {
    this.loading.set(true);
    const f = this.filterForm.getRawValue();
    this.api.list({
      page: this.pageIndex(), size: this.pageSize(),
      sort: this.sortControl.value,
      q: f.q || undefined, department: f.department || undefined,
      minSalary: f.minSalary ?? null, maxSalary: f.maxSalary ?? null
    }).subscribe({
      next: p => { this.pageData.set(p); this.loading.set(false); },
      error: ()  => this.loading.set(false)
    });
  }

  applyFilters(): void { this.pageIndex.set(0); this.reload(); }
  onSortChange(): void { this.pageIndex.set(0); this.reload(); }
  onPageSizeChange(n: number): void { this.pageSize.set(n); this.pageIndex.set(0); this.reload(); }
  setPage(i: number): void { this.pageIndex.set(i); this.reload(); }
  prev(): void { if (this.pageData()?.first) return; this.pageIndex.update(i => i - 1); this.reload(); }
  next(): void { if (this.pageData()?.last)  return; this.pageIndex.update(i => i + 1); this.reload(); }

  pageNumbers(): number[] {
    const p = this.pageData();
    if (!p) return [];
    const start = Math.max(0, Math.min(p.number - 2, p.totalPages - 5));
    return Array.from({ length: Math.min(5, p.totalPages) }, (_, i) => start + i);
  }

  // ── View modal ──────────────────────────────────────────────────────────────

  openView(e: Employee): void  { this.view.set(e); }
  closeView(): void            { this.view.set(null); }

  // ── Add / Edit modal ────────────────────────────────────────────────────────

  openForm(e?: Employee): void {
    this.modal.set({ open: true, editingId: e?.id ?? null, error: null });
    if (e) {
      this.empForm.patchValue({
        ...e,
        department: e.department ?? '',
        salary: e.salary != null && !Number.isNaN(Number(e.salary)) ? Number(e.salary) : null
      });
    } else {
      this.empForm.reset({ username: '', email: '', age: null, mobile: '', firstName: '', lastName: '', department: '', salary: null });
    }
  }

  closeForm(): void { this.modal.update(m => ({ ...m, open: false })); }

  save(): void {
    this.modal.update(m => ({ ...m, error: null }));
    if (this.empForm.invalid) { this.empForm.markAllAsTouched(); return; }
    const raw = this.empForm.getRawValue();
    const body: Employee = {
      ...raw,
      age: Number(raw.age),
      department: raw.department?.trim() || undefined,
      salary: raw.salary != null && !Number.isNaN(Number(raw.salary)) ? Number(raw.salary) : undefined
    };
    const id   = this.modal().editingId;
    const verb = id == null ? 'added' : 'updated';
    (id == null ? this.api.create(body) : this.api.update(id, body)).subscribe({
      next: () => { this.closeForm(); this.reload(); this.toast.success(`Employee ${verb} successfully`); },
      error: err => this.modal.update(m => ({ ...m, error: this.formatErr(err?.error) }))
    });
  }

  deleteRow(e: Employee): void {
    if (!e.id || !window.confirm(`Delete "${e.firstName} ${e.lastName}"?`)) return;
    this.api.delete(e.id).subscribe({
      next: () => { this.reload(); this.toast.success(`"${e.firstName} ${e.lastName}" deleted`); },
      error: err => this.toast.error(this.formatErr(err?.error))
    });
  }


  // ── Exports ─────────────────────────────────────────────────────────────────

  exportPdf(): void {
    const f = this.filterForm.getRawValue();
    this.api.exportPdf(f.q || undefined, f.department || undefined, f.minSalary, f.maxSalary).subscribe({
      next: blob => { this.saveBlob(blob, 'employees.pdf');  this.toast.info('PDF downloaded'); },
      error: () => this.toast.error('PDF export failed')
    });
  }

  exportExcel(): void {
    const f = this.filterForm.getRawValue();
    this.api.exportExcel(f.q || undefined, f.department || undefined, f.minSalary, f.maxSalary).subscribe({
      next: blob => { this.saveBlob(blob, 'employees.xlsx'); this.toast.info('Excel downloaded'); },
      error: () => this.toast.error('Excel export failed')
    });
  }

  // ── Clipboard ───────────────────────────────────────────────────────────────

  copy(value: string, label: string): void {
    navigator.clipboard.writeText(value).then(() => this.toast.success(`${label} copied`));
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private saveBlob(blob: Blob, filename: string): void {
    const a = Object.assign(document.createElement('a'), { href: URL.createObjectURL(blob), download: filename });
    a.click();
    URL.revokeObjectURL(a.href);
  }

  private formatErr(p: unknown): string {
    if (!p || typeof p !== 'object') return 'Request failed';
    const o = p as Record<string, unknown>;
    if (typeof o['message'] === 'string') return o['message'];
    const errs = o['errors'];
    if (errs && typeof errs === 'object') {
      const v = Object.values(errs as Record<string, string>)[0];
      return typeof v === 'string' ? v : 'Validation failed';
    }
    return 'Request failed';
  }
}
