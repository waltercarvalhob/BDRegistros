import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { Papel, UsuarioFiltro, UsuarioResponse, UsuarioService } from '../core/usuario.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.scss',
})
export class UsuariosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  usuarios: UsuarioResponse[] = [];
  carregando = false;
  erro: string | null = null;
  sucesso: string | null = null;

  mostrarForm = false;
  editando: UsuarioResponse | null = null;
  salvando = false;
  erroForm: string | null = null;

  readonly usuarioLogado = this.authService.getUsuarioLogado();

  readonly filtroForm = this.fb.group({
    login: [''],
    nomeCompleto: [''],
    papel: [''],
    ativo: [''],
  });

  readonly form = this.fb.group({
    login: ['', [Validators.required, Validators.maxLength(60)]],
    senha: ['', [Validators.minLength(8)]],
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    papel: ['OPERADOR', Validators.required],
  });

  constructor() {
    this.buscar();
  }

  buscar(): void {
    this.carregando = true;
    this.erro = null;

    const v = this.filtroForm.getRawValue();
    const filtro: UsuarioFiltro = {
      login: v.login || undefined,
      nomeCompleto: v.nomeCompleto || undefined,
      papel: (v.papel || undefined) as Papel | undefined,
      ativo: v.ativo === '' ? null : v.ativo === 'true',
    };

    this.usuarioService.listar(filtro).subscribe({
      next: (resposta) => {
        this.usuarios = resposta;
        this.carregando = false;
      },
      error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erro = mensagem)),
    });
  }

  abrirNovo(): void {
    this.editando = null;
    this.erroForm = null;
    this.form.reset({ login: '', senha: '', nomeCompleto: '', papel: 'OPERADOR' });
    this.form.get('login')?.enable();
    this.form.get('senha')?.setValidators([Validators.required, Validators.minLength(8)]);
    this.form.get('senha')?.updateValueAndValidity();
    this.mostrarForm = true;
  }

  abrirEdicao(usuario: UsuarioResponse): void {
    this.editando = usuario;
    this.erroForm = null;
    this.form.reset({ login: usuario.login, senha: '', nomeCompleto: usuario.nomeCompleto, papel: usuario.papel });
    this.form.get('login')?.disable();
    this.form.get('senha')?.setValidators([Validators.minLength(8)]);
    this.form.get('senha')?.updateValueAndValidity();
    this.mostrarForm = true;
  }

  cancelar(): void {
    this.mostrarForm = false;
    this.editando = null;
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    this.salvando = true;
    this.erroForm = null;

    const operacao = this.editando
      ? this.usuarioService.atualizar(this.editando.id, {
          nomeCompleto: v.nomeCompleto!,
          papel: v.papel as Papel,
          senha: v.senha || undefined,
        })
      : this.usuarioService.cadastrar({
          login: v.login!,
          senha: v.senha!,
          nomeCompleto: v.nomeCompleto!,
          papel: v.papel as Papel,
        });

    operacao.subscribe({
      next: () => {
        this.salvando = false;
        this.sucesso = this.editando ? 'Usuario atualizado com sucesso.' : 'Usuario cadastrado com sucesso.';
        this.mostrarForm = false;
        this.editando = null;
        this.buscar();
      },
      error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erroForm = mensagem)),
      complete: () => (this.salvando = false),
    });
  }

  desativar(usuario: UsuarioResponse): void {
    if (!confirm(`Desativar o usuario "${usuario.login}"? Ele deixara de conseguir entrar no sistema.`)) {
      return;
    }

    this.erro = null;
    this.usuarioService.desativar(usuario.id).subscribe({
      next: () => {
        this.sucesso = 'Usuario desativado com sucesso.';
        this.buscar();
      },
      error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erro = mensagem)),
    });
  }

  private tratarErroSessao(resposta: any, aplicar: (mensagem: string) => void): void {
    this.carregando = false;
    this.salvando = false;
    if (resposta?.status === 401 || resposta?.status === 403) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }
    aplicar(resposta?.error?.mensagem ?? 'Nao foi possivel concluir a operacao.');
  }
}
