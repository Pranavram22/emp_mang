import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StatsService, Stats, DeptCount } from '../../core/stats.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
<div class="page-wrap">
  <div class="section-title">Dashboard</div>
  <div class="section-sub">Welcome back, {{ auth.username() }} — here's a quick overview</div>

  @if (loading()) {
    <div class="text-center py-5 text-muted fs-sm">
      <div class="spinner-border spinner-border-sm me-2" role="status"></div>Loading stats...
    </div>
  }

  @if (!loading() && stats()) {
    <div class="row g-3 mb-4">
      @for (card of statCards(); track card.label) {
        <div class="col-md-3 col-sm-6">
          <div class="stat-card">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
        </div>
      }
    </div>

    <div class="row g-3 mb-3">
      <div class="col-md-6">
        <div class="card p-3">
          <div class="fs-xs text-muted mb-1">HIGHEST PAID EMPLOYEE</div>
          <div style="font-size:16px;font-weight:bold;">{{ stats()!.highestPaidName }}</div>
          <div class="fs-sm text-secondary mt-1">Salary: {{ stats()!.highestSalary | number:'1.0-0' }}</div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card p-3">
          <div class="fs-xs text-muted mb-2">QUICK ACTIONS</div>
          <div class="d-flex flex-wrap gap-2">
            <a routerLink="/employees" class="btn btn-dark btn-sm">View Employees</a>
            @if (auth.isAdmin()) {
              <a routerLink="/employees" class="btn btn-outline-secondary btn-sm">Export Data</a>
              <a routerLink="/admin/users" class="btn btn-outline-secondary btn-sm">Manage Users</a>
            }
          </div>
        </div>
      </div>
    </div>

    @if (deptBreakdown().length > 0) {
      <div class="card p-3 mb-3">
        <div class="fs-xs text-muted mb-2">EMPLOYEES BY DEPARTMENT</div>
        <table class="table table-sm table-bordered mb-0">
          <thead><tr><th class="fs-xs">Department</th><th class="fs-xs" style="width:80px;text-align:right;">Count</th><th class="fs-xs">Share</th></tr></thead>
          <tbody>
            @for (d of deptBreakdown(); track d.department) {
              <tr>
                <td class="fs-sm">{{ d.department }}</td>
                <td class="fs-sm" style="text-align:right;font-weight:bold;">{{ d.count }}</td>
                <td class="fs-xs">
                  <div class="d-flex align-items-center gap-2">
                    <div style="flex:1;background:#eee;border-radius:3px;height:8px;">
                      <div style="background:#1a1a2e;height:8px;border-radius:3px;"
                           [style.width.%]="stats()!.totalEmployees ? d.count / stats()!.totalEmployees * 100 : 0"></div>
                    </div>
                    <span class="text-muted">{{ stats()!.totalEmployees ? (d.count / stats()!.totalEmployees * 100 | number:'1.0-0') : 0 }}%</span>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }

    <div class="card p-3">
      <div class="fs-xs text-muted mb-1">YOUR ACCOUNT</div>
      <div class="d-flex align-items-center gap-3">
        <div>
          <div style="font-size:15px;font-weight:bold;">{{ auth.username() }}</div>
          <div class="fs-xs text-muted">Logged in as <strong>{{ auth.isAdmin() ? 'Administrator' : 'User' }}</strong></div>
        </div>
        <div class="fs-xs text-secondary" style="border-left:3px solid #dee2e6;padding-left:12px;">
          @if (auth.isAdmin()) { As Admin you can add, edit, delete employees and export records. }
          @else { As User you can view, add, and edit employee records. }
        </div>
      </div>
    </div>
  }
</div>`
})
export class DashboardComponent implements OnInit {
  private readonly statsService = inject(StatsService);
  readonly auth = inject(AuthService);

  readonly stats         = signal<Stats | null>(null);
  readonly deptBreakdown = signal<DeptCount[]>([]);
  readonly loading       = signal(true);

  statCards() {
    const s = this.stats();
    if (!s) return [];
    return [
      { label: 'Total Employees',  value: s.totalEmployees },
      { label: 'Departments',      value: s.totalDepartments },
      { label: 'Avg Salary',       value: new Intl.NumberFormat().format(Math.round(s.avgSalary)) },
      { label: 'Added This Month', value: s.addedThisMonth }
    ];
  }

  ngOnInit(): void {
    this.statsService.get().subscribe({ next: s => { this.stats.set(s); this.loading.set(false); }, error: () => this.loading.set(false) });
    this.statsService.deptBreakdown().subscribe(d => this.deptBreakdown.set(d));
  }
}
