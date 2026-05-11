import { Injectable, signal, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Toast { id: number; message: string; type: 'success' | 'error' | 'info'; }

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private counter = 0;

  success(message: string): void { this.show(message, 'success'); }
  error(message: string): void   { this.show(message, 'error'); }
  info(message: string): void    { this.show(message, 'info'); }
  remove(id: number): void       { this.toasts.update(ts => ts.filter(t => t.id !== id)); }

  private show(message: string, type: Toast['type']): void {
    const id = ++this.counter;
    this.toasts.update(ts => [...ts, { id, message, type }]);
    setTimeout(() => this.remove(id), 3500);
  }
}

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
