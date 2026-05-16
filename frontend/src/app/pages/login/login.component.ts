import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService, VALIDATION } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';

const usernameOrEmailValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = (control.value ?? '').toString().trim();
  if (!value) {
    return null;
  }
  const usernameRegex = new RegExp(VALIDATION.usernamePattern);
  if (usernameRegex.test(value)) {
    return null;
  }
  const emailError = Validators.email({ value } as AbstractControl);
  return emailError ? { usernameOrEmail: true } : null;
};

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
<div class="auth-box">
  <h4>Login</h4>
  <p class="hint">Enter your username and password to continue</p>
  @if (errorMsg()) { <div class="alert alert-danger py-2 mb-3 fs-sm">{{ errorMsg() }}</div> }
  <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
    <div class="mb-3">
      <label class="form-label">Username or Email</label>
      <input class="form-control" formControlName="username" autocomplete="username" placeholder="Enter username or email" />
      @if (form.controls.username.invalid && form.controls.username.touched) {
        <small class="text-danger">Enter a valid username or email address</small>
      }
    </div>
    <div class="mb-4">
      <label class="form-label">Password</label>
      <input type="password" class="form-control" formControlName="password" autocomplete="current-password" placeholder="Enter password" />
      @if (form.controls.password.invalid && form.controls.password.touched) {
        <small class="text-danger">Minimum 6 characters</small>
      }
    </div>
    <button type="submit" class="btn btn-dark w-100">Login</button>
  </form>
  <hr />
  <p class="text-center mb-1 fs-sm">No account? <a routerLink="/register">Register here</a></p>
</div>`
})
export class LoginComponent {
  private readonly auth  = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast  = inject(ToastService);
  readonly errorMsg = signal<string | null>(null);
  readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(120), usernameOrEmailValidator]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]]
  });

  submit(): void {
    this.errorMsg.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const raw = this.form.getRawValue();
    const body = { username: raw.username.trim(), password: raw.password };
    this.auth.login(body).subscribe({
      next: res => { this.auth.setSession(res); this.toast.success(`Welcome back, ${res.username}!`); void this.router.navigate(['/employees']); },
      error: err => this.errorMsg.set(err?.error?.message ?? 'Unable to log in. Demo: admin / admin123 or user1 / user123.')
    });
  }
}
