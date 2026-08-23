import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { cpfValidator } from '../core/cpf.validator';
import { AuthService } from '../core/auth.service';
import { TitularResponse, TitularService } from '../core/titular.service';

@Component({
  selector: 'app-busca',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './busca.component.html',
  styleUrl: './busca.component.scss',
})
export class BuscaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly titularService = inject(TitularService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  buscando = false;
  erro: string | null = null;
  resultado: TitularResponse | null = null;

  readonly form = this.fb.group({
    cpf: ['', [Validators.required, cpfValidator()]],
  });

  readonly usuarioLogado = this.authService.getUsuarioLogado();

  buscar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.buscando = true;
    this.erro = null;
    this.resultado = null;

    this.titularService.buscarPorCpf(this.form.value.cpf!).subscribe({
      next: (resposta) => {
        this.resultado = resposta;
        this.buscando = false;
      },
      error: (resposta) => {
        this.buscando = false;
        if (resposta?.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          return;
        }
        this.erro = resposta?.error?.mensagem ?? 'Nao foi possivel concluir a busca.';
      },
    });
  }

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
