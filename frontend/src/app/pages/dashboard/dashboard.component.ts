import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StatsService, Stats } from '../../core/stats.service';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private readonly statsService = inject(StatsService);
  readonly auth = inject(AuthService);

  readonly stats = signal<Stats | null>(null);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.statsService.get().subscribe({
      next: (s) => { this.stats.set(s); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
