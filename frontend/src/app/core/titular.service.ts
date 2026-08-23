import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface EnderecoPayload {
  logradouro: string;
  numero?: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
  cep?: string;
}

export interface TitularCadastroPayload {
  nomeCompleto: string;
  cpf: string;
  tituloEleitor?: string;
  telefone: string;
  dataNascimento?: string;
  endereco: EnderecoPayload;
  finalidadeConsentimento: string;
  canalConsentimento: 'PRESENCIAL' | 'DIGITAL' | 'TELEFONE';
  agenteResponsavel?: string;
  consentimentoConfirmado: boolean;
}

export interface TitularResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  tituloEleitor?: string;
  telefone?: string;
  dataNascimento?: string;
  status: string;
  cidade?: string;
  estado?: string;
}

@Injectable({ providedIn: 'root' })
export class TitularService {
  private readonly baseUrl = '/api/titulares';

  constructor(private readonly http: HttpClient) {}

  cadastrar(payload: TitularCadastroPayload): Observable<TitularResponse> {
    return this.http.post<TitularResponse>(this.baseUrl, payload);
  }

  buscarPorCpf(cpf: string): Observable<TitularResponse> {
    const digitos = (cpf || '').replace(/\D/g, '');
    return this.http.get<TitularResponse>(`${this.baseUrl}/${digitos}`);
  }
}
