import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<Toast[]>([]);
  private counter = 0;

  success(message: string): void { this.show(message, 'success'); }
  error(message: string): void   { this.show(message, 'error'); }
  info(message: string): void    { this.show(message, 'info'); }

  remove(id: number): void {
    this.toasts.update((ts) => ts.filter((t) => t.id !== id));
  }

  private show(message: string, type: Toast['type']): void {
    const id = ++this.counter;
    this.toasts.update((ts) => [...ts, { id, message, type }]);
    setTimeout(() => this.remove(id), 3500);
  }
}
