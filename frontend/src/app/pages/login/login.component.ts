import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { VALIDATION } from '../../core/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly errorMsg = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: [
      '',
      [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
        Validators.pattern(VALIDATION.usernamePattern)
      ]
    ],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]]
  });

  submit(): void {
    this.errorMsg.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.auth.setSession(res);
        void this.router.navigate(['/employees']);
      },
      error: () =>
        this.errorMsg.set('Invalid username or password. Demo: admin / admin123 or user1 / user123.')
    });
  }
}
