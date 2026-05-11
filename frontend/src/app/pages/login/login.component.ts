import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { VALIDATION } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
<div class="auth-box">
  <h4>Login</h4>
  <p class="hint">Enter your username and password to continue</p>
  @if (errorMsg()) { <div class="alert alert-danger py-2 mb-3 fs-sm">{{ errorMsg() }}</div> }
  <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
    <div class="mb-3">
      <label class="form-label">Username</label>
      <input class="form-control" formControlName="username" autocomplete="username" placeholder="Enter username" />
      @if (form.controls.username.invalid && form.controls.username.touched) {
        <small class="text-danger">3–50 characters, letters/digits/underscore</small>
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
  <p class="text-center text-muted mb-0" style="font-size:11px;">Test: admin / admin123 &nbsp;|&nbsp; user1 / user123</p>
</div>`
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly errorMsg = signal<string | null>(null);
  readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(VALIDATION.usernamePattern)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]]
  });

  submit(): void {
    this.errorMsg.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.auth.login(this.form.getRawValue()).subscribe({
      next: res => { this.auth.setSession(res); void this.router.navigate(['/employees']); },
      error: () => this.errorMsg.set('Invalid username or password. Demo: admin / admin123 or user1 / user123.')
    });
  }
}
