import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AppUser } from '../../core/admin.service';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly users = signal<AppUser[]>([]);
  readonly loading = signal(true);
  readonly saving = signal<number | null>(null);
  readonly adminCount = computed(() => this.users().filter((u) => u.role === 'ADMIN').length);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.adminService.listUsers().subscribe({
      next: (u) => { this.users.set(u); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  toggleRole(u: AppUser): void {
    const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!window.confirm(`Change ${u.username}'s role to ${newRole}?`)) return;
    this.saving.set(u.id);
    this.adminService.changeRole(u.id, newRole).subscribe({
      next: (updated) => {
        this.users.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
        this.saving.set(null);
        this.toast.success(`${updated.username} is now ${updated.role}`);
      },
      error: (err) => {
        this.saving.set(null);
        this.toast.error(err?.error?.message ?? 'Failed to change role');
      }
    });
  }

  deleteUser(u: AppUser): void {
    if (!window.confirm(`Delete account "${u.username}"? This cannot be undone.`)) return;
    this.adminService.deleteUser(u.id).subscribe({
      next: () => {
        this.users.update((list) => list.filter((x) => x.id !== u.id));
        this.toast.success(`Account "${u.username}" deleted`);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Failed to delete user')
    });
  }

  isSelf(u: AppUser): boolean {
    return u.username === this.auth.username();
  }
}
