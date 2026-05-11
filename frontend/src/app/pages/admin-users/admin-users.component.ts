import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AppUser } from '../../core/stats.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="page-wrap">
  <div class="section-title">User Management</div>
  <div class="section-sub">Manage app accounts and roles — Admin only</div>

  @if (loading()) {
    <div class="text-center py-5 text-muted fs-sm">
      <div class="spinner-border spinner-border-sm me-2" role="status"></div>Loading users...
    </div>
  }

  @if (!loading()) {
    <div class="card">
      <table class="table table-hover table-bordered mb-0">
        <thead>
          <tr><th>#</th><th>Username</th><th>Email</th><th>Role</th><th>Actions</th></tr>
        </thead>
        <tbody>
          @for (u of users(); track u.id) {
            <tr>
              <td class="text-muted">{{ u.id }}</td>
              <td>
                <strong>{{ u.username }}</strong>
                @if (isSelf(u)) { <span class="badge bg-secondary ms-1 fs-xs">You</span> }
              </td>
              <td>{{ u.email }}</td>
              <td>
                @if (u.role === 'ADMIN') { <span class="badge bg-warning text-dark">Admin</span> }
                @else { <span class="badge bg-secondary">User</span> }
              </td>
              <td>
                @if (!isSelf(u)) {
                  <button class="btn btn-sm me-1"
                    [class]="u.role === 'ADMIN' ? 'btn-outline-secondary' : 'btn-outline-warning'"
                    [disabled]="saving() === u.id" (click)="toggleRole(u)">
                    {{ saving() === u.id ? '...' : (u.role === 'ADMIN' ? 'Demote to User' : 'Promote to Admin') }}
                  </button>
                  <button class="btn btn-sm btn-outline-danger" (click)="deleteUser(u)">Delete</button>
                } @else {
                  <span class="text-muted fs-xs">— current session</span>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
    <div class="mt-3 fs-xs text-muted">
      Total: {{ users().length }} &nbsp;|&nbsp; Admins: {{ adminCount() }} &nbsp;|&nbsp; Users: {{ users().length - adminCount() }}
    </div>
  }
</div>`
})
export class AdminUsersComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly toast        = inject(ToastService);
  readonly auth                 = inject(AuthService);

  readonly users      = signal<AppUser[]>([]);
  readonly loading    = signal(true);
  readonly saving     = signal<number | null>(null);
  readonly adminCount = computed(() => this.users().filter(u => u.role === 'ADMIN').length);

  ngOnInit(): void {
    this.adminService.listUsers().subscribe({ next: u => { this.users.set(u); this.loading.set(false); }, error: () => this.loading.set(false) });
  }

  isSelf(u: AppUser): boolean { return u.username === this.auth.username(); }

  toggleRole(u: AppUser): void {
    const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!window.confirm(`Change ${u.username}'s role to ${newRole}?`)) return;
    this.saving.set(u.id);
    this.adminService.changeRole(u.id, newRole).subscribe({
      next: updated => { this.users.update(list => list.map(x => x.id === updated.id ? updated : x)); this.saving.set(null); this.toast.success(`${updated.username} is now ${updated.role}`); },
      error: err => { this.saving.set(null); this.toast.error(err?.error?.message ?? 'Failed to change role'); }
    });
  }

  deleteUser(u: AppUser): void {
    if (!window.confirm(`Delete account "${u.username}"?`)) return;
    this.adminService.deleteUser(u.id).subscribe({
      next: () => { this.users.update(list => list.filter(x => x.id !== u.id)); this.toast.success(`Account "${u.username}" deleted`); },
      error: err => this.toast.error(err?.error?.message ?? 'Failed to delete user')
    });
  }
}
