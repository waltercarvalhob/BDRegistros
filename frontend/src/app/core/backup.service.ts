import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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
  dryRun: boolean;
}

@Injectable({ providedIn: 'root' })
export class BackupService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/backup/titulares`;

  constructor(private readonly http: HttpClient) {}

  exportar(formato: FormatoBackup): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      params: { formato },
      responseType: 'blob',
    });
  }

  importar(file: File, formato?: FormatoBackup, dryRun = false): Observable<ImportResultado> {
    const formData = new FormData();
    formData.append('file', file);
    if (formato) {
      formData.append('formato', formato);
    }
    formData.append('dryRun', String(dryRun));
    return this.http.post<ImportResultado>(`${this.baseUrl}/import`, formData);
  }
}
