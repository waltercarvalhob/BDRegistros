package br.com.bdregistros.controller;

import br.com.bdregistros.dto.TitularAtualizacaoRequest;
import br.com.bdregistros.dto.TitularCadastroRequest;
import br.com.bdregistros.dto.TitularResponse;
import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;
import br.com.bdregistros.service.TitularService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/titulares")
public class TitularController {

    private final TitularService titularService;

    public TitularController(TitularService titularService) {
        this.titularService = titularService;
    }

    /**
     * Publico: preenchido pelo proprio titular (ou pelo agente, com o
     * titular presente) confirmando o consentimento no ato do cadastro.
     */
    @PostMapping
    public ResponseEntity<TitularResponse> cadastrar(@Valid @RequestBody TitularCadastroRequest request) {
        Titular titular = titularService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TitularResponse.from(titular));
    }

    /**
     * Restrito a usuarios internos autenticados. Cada titular retornado
     * fica registrado em log_acesso. Sem filtro de status, EXCLUIDO fica
     * de fora por padrao.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Page<TitularResponse>> listar(@RequestParam(required = false) String cpf,
                                                          @RequestParam(required = false) String nomeCompleto,
                                                          @RequestParam(required = false) String cidade,
                                                          @RequestParam(required = false) String tituloEleitor,
                                                          @RequestParam(required = false) StatusTitular status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          Principal principal) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("nomeCompleto"));
        Page<TitularResponse> resultado = titularService
                .listar(cpf, nomeCompleto, cidade, tituloEleitor, status, pageable, principal.getName())
                .map(TitularResponse::from);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<TitularResponse> obter(@PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(TitularResponse.from(titularService.buscarPorId(id, principal.getName())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<TitularResponse> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody TitularAtualizacaoRequest request,
                                                       Principal principal) {
        Titular titular = titularService.atualizar(id, request, principal.getName());
        return ResponseEntity.ok(TitularResponse.from(titular));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id, Principal principal) {
        titularService.excluir(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/consentimento")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> revogarConsentimento(@PathVariable UUID id, Principal principal) {
        titularService.revogarConsentimento(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
