import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginPayload {
  login: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
}

export interface UsuarioLogado {
  login: string;
  papel: string;
}

const TOKEN_KEY = 'bdregistros_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {}

  login(payload: LoginPayload): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/auth/login', payload)
      .pipe(tap((resposta) => sessionStorage.setItem(TOKEN_KEY, resposta.token)));
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
  }

  getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUsuarioLogado(): UsuarioLogado | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }

    try {
      const payloadBase64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(payloadBase64));
      return { login: payload.sub, papel: payload.papel };
    } catch {
      return null;
    }
  }
}
