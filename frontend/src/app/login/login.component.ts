import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  enviando = false;
  erro: string | null = null;

  readonly form = this.fb.group({
    login: ['', Validators.required],
    senha: ['', Validators.required],
  });

  entrar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando = true;
    this.erro = null;

    const { login, senha } = this.form.getRawValue();
    this.authService.login({ login: login!, senha: senha! }).subscribe({
      next: () => {
        this.enviando = false;
        this.router.navigate(['/busca']);
      },
      error: (resposta) => {
        this.enviando = false;
        this.erro = resposta?.error?.mensagem ?? 'Nao foi possivel entrar. Tente novamente.';
      },
    });
  }
}
