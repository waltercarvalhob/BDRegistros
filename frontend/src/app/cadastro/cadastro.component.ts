import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { cpfValidator } from '../core/cpf.validator';
import { TitularService } from '../core/titular.service';

const FINALIDADE_PADRAO = 'Cadastro para Tarifa Social de Energia Eletrica / Agua';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.scss',
})
export class CadastroComponent {
  private readonly fb = inject(FormBuilder);
  private readonly titularService = inject(TitularService);

  enviando = false;
  sucesso = false;
  erro: string | null = null;

  readonly finalidade = FINALIDADE_PADRAO;

  readonly form = this.fb.group({
    nomeCompleto: ['', [Validators.required, Validators.maxLength(150)]],
    cpf: ['', [Validators.required, cpfValidator()]],
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
    canalConsentimento: ['PRESENCIAL', Validators.required],
    agenteResponsavel: [''],
    consentimentoConfirmado: [false, Validators.requiredTrue],
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    this.enviando = true;
    this.erro = null;

    this.titularService
      .cadastrar({
        nomeCompleto: v.nomeCompleto!,
        cpf: v.cpf!,
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
        finalidadeConsentimento: this.finalidade,
        canalConsentimento: v.canalConsentimento as 'PRESENCIAL' | 'DIGITAL' | 'TELEFONE',
        agenteResponsavel: v.agenteResponsavel || undefined,
        consentimentoConfirmado: !!v.consentimentoConfirmado,
      })
      .subscribe({
        next: () => {
          this.sucesso = true;
          this.enviando = false;
          this.form.reset({ canalConsentimento: 'PRESENCIAL' });
        },
        error: (resposta) => {
          this.erro = resposta?.error?.mensagem ?? 'Nao foi possivel concluir o cadastro. Tente novamente.';
          this.enviando = false;
        },
      });
  }
}
