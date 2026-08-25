import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type StatusTitular = 'ATIVO' | 'INATIVO' | 'EXCLUIDO';

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

export interface TitularAtualizacaoPayload {
  nomeCompleto: string;
  tituloEleitor?: string;
  telefone: string;
  dataNascimento?: string;
  endereco: EnderecoPayload;
}

export interface TitularResponse {
  id: string;
  nomeCompleto: string;
  cpf: string;
  tituloEleitor?: string;
  telefone?: string;
  dataNascimento?: string;
  status: StatusTitular;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  cep?: string;
}

export interface TitularFiltro {
  cpf?: string;
  nomeCompleto?: string;
  cidade?: string;
  tituloEleitor?: string;
  status?: StatusTitular | '';
  page?: number;
  size?: number;
}

export interface TitularPagina {
  content: TitularResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class TitularService {
  private readonly baseUrl = '/api/titulares';

  constructor(private readonly http: HttpClient) {}

  cadastrar(payload: TitularCadastroPayload): Observable<TitularResponse> {
    return this.http.post<TitularResponse>(this.baseUrl, payload);
  }

  listar(filtro: TitularFiltro): Observable<TitularPagina> {
    let params = new HttpParams();
    if (filtro.cpf) {
      params = params.set('cpf', filtro.cpf.replace(/\D/g, ''));
    }
    if (filtro.nomeCompleto) {
      params = params.set('nomeCompleto', filtro.nomeCompleto);
    }
    if (filtro.cidade) {
      params = params.set('cidade', filtro.cidade);
    }
    if (filtro.tituloEleitor) {
      params = params.set('tituloEleitor', filtro.tituloEleitor);
    }
    if (filtro.status) {
      params = params.set('status', filtro.status);
    }
    params = params.set('page', String(filtro.page ?? 0));
    params = params.set('size', String(filtro.size ?? 20));
    return this.http.get<TitularPagina>(this.baseUrl, { params });
  }

  obter(id: string): Observable<TitularResponse> {
    return this.http.get<TitularResponse>(`${this.baseUrl}/${id}`);
  }

  atualizar(id: string, payload: TitularAtualizacaoPayload): Observable<TitularResponse> {
    return this.http.put<TitularResponse>(`${this.baseUrl}/${id}`, payload);
  }

  excluir(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
