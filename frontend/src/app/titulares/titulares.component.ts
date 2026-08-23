import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { StatusTitular, TitularFiltro, TitularResponse, TitularService } from '../core/titular.service';

@Component({
  selector: 'app-titulares',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './titulares.component.html',
  styleUrl: './titulares.component.scss',
})
export class TitularesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly titularService = inject(TitularService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  titulares: TitularResponse[] = [];
  buscando = false;
  jaBuscou = false;
  erro: string | null = null;
  sucesso: string | null = null;

  mostrarForm = false;
  editando: TitularResponse | null = null;
  salvando = false;
  erroForm: string | null = null;

  readonly usuarioLogado = this.authService.getUsuarioLogado();

  readonly filtroForm = this.fb.group({
    cpf: [''],
    nomeCompleto: [''],
    cidade: [''],
    status: [''],
  });

  readonly form = this.fb.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    tituloEleitor: [''],
    telefone: ['', Validators.required],
    dataNascimento: [''],
    logradouro: ['', Validators.required],
    numero: [''],
    complemento: [''],
    bairro: ['', Validators.required],
    cidade: ['', Validators.required],
    estado: ['', [Validators.required, Validators.maxLength(2)]],
    cep: [''],
  });

  buscar(): void {
    const v = this.filtroForm.getRawValue();
    const filtro: TitularFiltro = {
      cpf: v.cpf || undefined,
      nomeCompleto: v.nomeCompleto || undefined,
      cidade: v.cidade || undefined,
      status: (v.status || undefined) as StatusTitular | undefined,
    };

    if (!filtro.cpf && !filtro.nomeCompleto && !filtro.cidade && !filtro.status) {
      this.erro = 'Preencha ao menos um filtro (CPF, nome, cidade ou status) para pesquisar.';
      return;
    }

    this.buscando = true;
    this.erro = null;

    this.titularService.listar(filtro).subscribe({
      next: (resposta) => {
        this.titulares = resposta;
        this.buscando = false;
        this.jaBuscou = true;
      },
      error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erro = mensagem)),
    });
  }

  abrirEdicao(titular: TitularResponse): void {
    this.editando = titular;
    this.erroForm = null;
    this.form.reset({
      nomeCompleto: titular.nomeCompleto,
      tituloEleitor: titular.tituloEleitor || '',
      telefone: titular.telefone || '',
      dataNascimento: titular.dataNascimento || '',
      logradouro: titular.logradouro || '',
      numero: titular.numero || '',
      complemento: titular.complemento || '',
      bairro: titular.bairro || '',
      cidade: titular.cidade || '',
      estado: titular.estado || '',
      cep: titular.cep || '',
    });
    this.mostrarForm = true;
  }

  cancelar(): void {
    this.mostrarForm = false;
    this.editando = null;
  }

  salvar(): void {
    if (!this.editando) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    this.salvando = true;
    this.erroForm = null;

    this.titularService
      .atualizar(this.editando.id, {
        nomeCompleto: v.nomeCompleto!,
        tituloEleitor: v.tituloEleitor || undefined,
        telefone: v.telefone!,
        dataNascimento: v.dataNascimento || undefined,
        endereco: {
          logradouro: v.logradouro!,
          numero: v.numero || undefined,
          complemento: v.complemento || undefined,
          bairro: v.bairro!,
          cidade: v.cidade!,
          estado: v.estado!,
          cep: v.cep || undefined,
        },
      })
      .subscribe({
        next: () => {
          this.salvando = false;
          this.sucesso = 'Titular atualizado com sucesso.';
          this.mostrarForm = false;
          this.editando = null;
          this.buscar();
        },
        error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erroForm = mensagem)),
      });
  }

  excluir(titular: TitularResponse): void {
    if (!confirm(`Excluir o cadastro de "${titular.nomeCompleto}"? Essa acao pode ser revertida apenas por um administrador diretamente no banco.`)) {
      return;
    }

    this.erro = null;
    this.titularService.excluir(titular.id).subscribe({
      next: () => {
        this.sucesso = 'Titular excluido com sucesso.';
        this.buscar();
      },
      error: (resposta) => this.tratarErroSessao(resposta, (mensagem) => (this.erro = mensagem)),
    });
  }

  private tratarErroSessao(resposta: any, aplicar: (mensagem: string) => void): void {
    this.buscando = false;
    this.salvando = false;
    if (resposta?.status === 401) {
      this.authService.logout();
      this.router.navigate(['/login']);
      return;
    }
    aplicar(resposta?.error?.mensagem ?? 'Nao foi possivel concluir a operacao.');
  }
}
