import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  constructor(
    public readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  get isAdmin(): boolean {
    return this.authService.getUsuarioLogado()?.papel === 'ADMIN';
  }

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
