import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type FormatoBackup = 'csv' | 'xlsx';

export interface ImportErro {
  linha: number;
  mensagem: string;
}

export interface ImportResultado {
  totalLinhas: number;
  criados: number;
  atualizados: number;
  erros: ImportErro[];
}

@Injectable({ providedIn: 'root' })
export class BackupService {
  private readonly baseUrl = '/api/backup/titulares';

  constructor(private readonly http: HttpClient) {}

  exportar(formato: FormatoBackup): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      params: { formato },
      responseType: 'blob',
    });
  }

  importar(file: File, formato?: FormatoBackup): Observable<ImportResultado> {
    const formData = new FormData();
    formData.append('file', file);
    if (formato) {
      formData.append('formato', formato);
    }
    return this.http.post<ImportResultado>(`${this.baseUrl}/import`, formData);
  }
}
