package br.com.bdregistros.controller;

import br.com.bdregistros.dto.ImportResultadoResponse;
import br.com.bdregistros.service.TitularExportService;
import br.com.bdregistros.service.TitularImportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;

/**
 * Backup em massa dos titulares (exportar/restaurar). Restrito a ADMIN:
 * exportar/importar em lote expoe muito mais dados pessoais de uma vez do
 * que a consulta por CPF (essa sim liberada tambem para OPERADOR).
 */
@RestController
@RequestMapping("/api/backup/titulares")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final TitularExportService exportService;
    private final TitularImportService importService;

    public BackupController(TitularExportService exportService, TitularImportService importService) {
        this.exportService = exportService;
        this.importService = importService;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportar(@RequestParam(defaultValue = "csv") String formato, Principal principal) {
        byte[] conteudo;
        MediaType tipo;
        String extensao;

        if ("xlsx".equalsIgnoreCase(formato)) {
            conteudo = exportService.exportarXlsx(principal.getName());
            tipo = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            extensao = "xlsx";
        } else {
            conteudo = exportService.exportarCsv(principal.getName());
            tipo = MediaType.parseMediaType("text/csv");
            extensao = "csv";
        }

        String nomeArquivo = "titulares-" + LocalDate.now() + "." + extensao;
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nomeArquivo).build().toString())
                .body(conteudo);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultadoResponse> importar(@RequestPart("file") MultipartFile file,
                                                              @RequestParam(required = false) String formato,
                                                              Principal principal) {
        ImportResultadoResponse resultado = importService.importar(file, formato, principal.getName());
        return ResponseEntity.ok(resultado);
    }
}
