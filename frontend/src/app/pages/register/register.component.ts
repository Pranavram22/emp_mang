import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { VALIDATION } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
<div class="auth-box">
  <h4>Register</h4>
  <p class="hint">Create an account — you will be assigned the User role</p>
  @if (errorMsg()) { <div class="alert alert-danger py-2 mb-3 fs-sm">{{ errorMsg() }}</div> }
  <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
    <div class="mb-3">
      <label class="form-label">Username</label>
      <input class="form-control" formControlName="username" autocomplete="username" placeholder="Choose a username" />
      @if (form.controls.username.invalid && form.controls.username.touched) {
        <small class="text-danger">3–50 characters, letters/digits/underscore</small>
      }
    </div>
    <div class="mb-3">
      <label class="form-label">Email</label>
      <input type="email" class="form-control" formControlName="email" autocomplete="email" placeholder="your@email.com" />
      @if (form.controls.email.invalid && form.controls.email.touched) {
        <small class="text-danger">Enter a valid email</small>
      }
    </div>
    <div class="mb-4">
      <label class="form-label">Password</label>
      <input type="password" class="form-control" formControlName="password" autocomplete="new-password" placeholder="Min 6 characters" />
      @if (form.controls.password.invalid && form.controls.password.touched) {
        <small class="text-danger">Minimum 6 characters</small>
      }
    </div>
    <button type="submit" class="btn btn-dark w-100">Create Account</button>
  </form>
  <p class="text-center mt-3 mb-0 fs-sm">Already have an account? <a routerLink="/login">Login</a></p>
</div>`
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly errorMsg = signal<string | null>(null);
  readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(VALIDATION.usernamePattern)]],
    email:    ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]]
  });

  submit(): void {
    this.errorMsg.set(null);
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.auth.register(this.form.getRawValue()).subscribe({
      next: res => { this.auth.setSession(res); void this.router.navigate(['/employees']); },
      error: err => this.errorMsg.set(err?.error?.message ?? 'Unable to register')
    });
  }
}
