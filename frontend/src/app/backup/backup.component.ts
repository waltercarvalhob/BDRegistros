import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { BackupService, FormatoBackup, ImportResultado } from '../core/backup.service';

@Component({
  selector: 'app-backup',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './backup.component.html',
  styleUrl: './backup.component.scss',
})
export class BackupComponent {
  private readonly backupService = inject(BackupService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly usuarioLogado = this.authService.getUsuarioLogado();

  exportando = false;
  erroExportacao: string | null = null;

  arquivoSelecionado: File | null = null;
  importando = false;
  erroImportacao: string | null = null;
  resultado: ImportResultado | null = null;

  exportar(formato: FormatoBackup): void {
    this.exportando = true;
    this.erroExportacao = null;

    this.backupService.exportar(formato).subscribe({
      next: (blob) => {
        this.exportando = false;
        const hoje = new Date().toISOString().slice(0, 10);
        this.baixarArquivo(blob, `titulares-${hoje}.${formato}`);
      },
      error: (resposta) => {
        this.exportando = false;
        if (resposta?.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          return;
        }
        this.erroExportacao = 'Nao foi possivel gerar o backup. Tente novamente.';
      },
    });
  }

  selecionarArquivo(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.arquivoSelecionado = input.files?.[0] ?? null;
    this.resultado = null;
    this.erroImportacao = null;
  }

  visualizar(): void {
    this.executarImportacao(true);
  }

  confirmar(): void {
    this.executarImportacao(false);
  }

  cancelarPreVisualizacao(): void {
    this.resultado = null;
  }

  private executarImportacao(dryRun: boolean): void {
    if (!this.arquivoSelecionado) {
      return;
    }

    this.importando = true;
    this.erroImportacao = null;

    this.backupService.importar(this.arquivoSelecionado, undefined, dryRun).subscribe({
      next: (resultado) => {
        this.importando = false;
        this.resultado = resultado;
        if (!dryRun) {
          this.arquivoSelecionado = null;
        }
      },
      error: (resposta) => {
        this.importando = false;
        this.resultado = null;
        if (resposta?.status === 401) {
          this.authService.logout();
          this.router.navigate(['/login']);
          return;
        }
        this.erroImportacao = resposta?.error?.mensagem ?? 'Nao foi possivel importar o arquivo.';
      },
    });
  }

  private baixarArquivo(blob: Blob, nomeArquivo: string): void {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nomeArquivo;
    link.click();
    URL.revokeObjectURL(url);
  }
}
