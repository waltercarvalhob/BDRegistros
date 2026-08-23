import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type Papel = 'ADMIN' | 'OPERADOR';

export interface UsuarioResponse {
  id: string;
  login: string;
  nomeCompleto: string;
  papel: Papel;
  ativo: boolean;
}

export interface UsuarioCadastroPayload {
  login: string;
  senha: string;
  nomeCompleto: string;
  papel: Papel;
}

export interface UsuarioAtualizacaoPayload {
  nomeCompleto: string;
  papel: Papel;
  senha?: string;
}

export interface UsuarioFiltro {
  login?: string;
  nomeCompleto?: string;
  papel?: Papel | '';
  ativo?: boolean | null;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly baseUrl = '/api/usuarios';

  constructor(private readonly http: HttpClient) {}

  listar(filtro: UsuarioFiltro): Observable<UsuarioResponse[]> {
    let params = new HttpParams();
    if (filtro.login) {
      params = params.set('login', filtro.login);
    }
    if (filtro.nomeCompleto) {
      params = params.set('nomeCompleto', filtro.nomeCompleto);
    }
    if (filtro.papel) {
      params = params.set('papel', filtro.papel);
    }
    if (filtro.ativo !== null && filtro.ativo !== undefined) {
      params = params.set('ativo', filtro.ativo);
    }
    return this.http.get<UsuarioResponse[]>(this.baseUrl, { params });
  }

  cadastrar(payload: UsuarioCadastroPayload): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(this.baseUrl, payload);
  }

  atualizar(id: string, payload: UsuarioAtualizacaoPayload): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${this.baseUrl}/${id}`, payload);
  }

  desativar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
