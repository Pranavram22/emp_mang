import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      @for (t of toast.toasts(); track t.id) {
        <div class="toast-item toast-{{ t.type }}" role="alert">
          <span class="toast-msg">{{ t.message }}</span>
          <button class="toast-close" (click)="toast.remove(t.id)">×</button>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  readonly toast = inject(ToastService);
}
