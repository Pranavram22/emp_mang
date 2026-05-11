import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth.service';
import { ToastService } from './core/toast.service';
import { ToastComponent } from './core/toast.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastComponent, ReactiveFormsModule, CommonModule],
  templateUrl: './app.component.html',
  styles: []
})
export class AppComponent {
  readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);

  readonly changePwOpen = signal(false);
  readonly changePwError = signal<string | null>(null);
  readonly changePwLoading = signal(false);

  readonly changePwForm = this.fb.nonNullable.group({
    currentPassword: ['', Validators.required],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  });

  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }

  openChangePw(): void {
    this.changePwForm.reset();
    this.changePwError.set(null);
    this.changePwOpen.set(true);
  }

  closeChangePw(): void {
    this.changePwOpen.set(false);
  }

  submitChangePw(): void {
    this.changePwError.set(null);
    if (this.changePwForm.invalid) {
      this.changePwForm.markAllAsTouched();
      return;
    }
    const { currentPassword, newPassword, confirmPassword } = this.changePwForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.changePwError.set('New passwords do not match');
      return;
    }
    this.changePwLoading.set(true);
    this.auth.changePassword(currentPassword, newPassword).subscribe({
      next: () => {
        this.changePwLoading.set(false);
        this.closeChangePw();
        this.toast.success('Password changed successfully');
      },
      error: (err) => {
        this.changePwLoading.set(false);
        const msg = err?.error?.message ?? 'Failed to change password';
        this.changePwError.set(msg);
      }
    });
  }
}
