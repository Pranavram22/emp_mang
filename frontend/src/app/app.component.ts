import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth.service';
import { ToastService, ToastComponent } from './core/toast.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent, ReactiveFormsModule, CommonModule],
  styles: [],
  template: `
<div class="main-navbar">
  <a class="brand-name" routerLink="/dashboard">
    Employee Database<span class="brand-sub">Management System</span>
  </a>
  @if (auth.isLoggedIn()) {
    <div class="d-flex align-items-center gap-1 mx-auto">
      <a class="nav-link-item" routerLink="/dashboard" routerLinkActive="active-link">Dashboard</a>
      <a class="nav-link-item" routerLink="/employees" routerLinkActive="active-link">Employees</a>
      @if (auth.isAdmin()) {
        <a class="nav-link-item nav-admin-link" routerLink="/admin/users" routerLinkActive="active-link">Users</a>
      }
    </div>
  }
  <div class="nav-right">
    @if (!auth.isLoggedIn()) {
      <a class="btn btn-outline-light btn-sm" routerLink="/login">Login</a>
      <a class="btn btn-light btn-sm" routerLink="/register">Register</a>
    } @else {
      <button class="user-name btn-plain" (click)="openChangePw()" title="Change password">{{ auth.username() }}</button>
      @if (auth.isAdmin()) { <span class="badge bg-warning text-dark">Admin</span> }
      @else { <span class="badge bg-secondary">User</span> }
      <button class="btn btn-outline-light btn-sm" (click)="logout()">Logout</button>
    }
  </div>
</div>

<router-outlet />
<app-toast />

@if (pw().open) {
  <div class="modal fade show d-block" tabindex="-1" aria-modal="true" role="dialog" (mousedown)="closeChangePw()">
    <div class="modal-dialog modal-dialog-centered" (mousedown)="$event.stopPropagation()">
      <div class="modal-content">
        <div class="modal-header modal-header-dark">
          <h5 class="modal-title text-white">Change Password</h5>
          <button type="button" class="btn-close btn-close-white" (click)="closeChangePw()"></button>
        </div>
        <div class="modal-body">
          @if (pw().error) { <div class="alert alert-danger py-2 fs-sm">{{ pw().error }}</div> }
          <div class="fs-xs text-muted mb-3">Logged in as <strong>{{ auth.username() }}</strong></div>
          <form [formGroup]="pwForm">
            <div class="mb-3">
              <label class="form-label">Current Password</label>
              <input type="password" class="form-control form-control-sm" formControlName="current" autocomplete="current-password" />
              @if (pwForm.controls.current.invalid && pwForm.controls.current.touched) { <small class="text-danger">Required</small> }
            </div>
            <div class="mb-3">
              <label class="form-label">New Password</label>
              <input type="password" class="form-control form-control-sm" formControlName="next" autocomplete="new-password" />
              @if (pwForm.controls.next.invalid && pwForm.controls.next.touched) { <small class="text-danger">Minimum 6 characters</small> }
            </div>
            <div class="mb-0">
              <label class="form-label">Confirm New Password</label>
              <input type="password" class="form-control form-control-sm" formControlName="confirm" autocomplete="new-password" />
              @if (pwForm.controls.confirm.invalid && pwForm.controls.confirm.touched) { <small class="text-danger">Required</small> }
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary btn-sm" (click)="closeChangePw()">Cancel</button>
          <button class="btn btn-dark btn-sm" (click)="submitChangePw()" [disabled]="pw().loading">
            {{ pw().loading ? 'Saving...' : 'Change Password' }}
          </button>
        </div>
      </div>
    </div>
  </div>
  <div class="modal-backdrop fade show"></div>
}
`
})
export class AppComponent {
  readonly auth  = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast  = inject(ToastService);

  readonly pw = signal({ open: false, loading: false, error: null as string | null });
  readonly pwForm = inject(FormBuilder).nonNullable.group({
    current: ['', Validators.required],
    next:    ['', [Validators.required, Validators.minLength(6)]],
    confirm: ['', Validators.required]
  });

  logout(): void { this.auth.logout(); void this.router.navigate(['/login']); }

  openChangePw(): void  { this.pwForm.reset(); this.pw.set({ open: true, loading: false, error: null }); }
  closeChangePw(): void { this.pw.update(s => ({ ...s, open: false })); }

  submitChangePw(): void {
    this.pw.update(s => ({ ...s, error: null }));
    if (this.pwForm.invalid) { this.pwForm.markAllAsTouched(); return; }
    const { current, next, confirm } = this.pwForm.getRawValue();
    if (next !== confirm) { this.pw.update(s => ({ ...s, error: 'New passwords do not match' })); return; }
    this.pw.update(s => ({ ...s, loading: true }));
    this.auth.changePassword(current, next).subscribe({
      next: () => { this.closeChangePw(); this.toast.success('Password changed successfully'); },
      error: err => this.pw.update(s => ({ ...s, loading: false, error: err?.error?.message ?? 'Failed to change password' }))
    });
  }
}
