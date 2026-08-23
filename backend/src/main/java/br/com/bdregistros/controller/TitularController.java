package br.com.bdregistros.controller;

import br.com.bdregistros.dto.TitularCadastroRequest;
import br.com.bdregistros.dto.TitularResponse;
import br.com.bdregistros.model.Titular;
import br.com.bdregistros.service.TitularService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * Restrito a usuarios internos autenticados. Cada consulta fica
     * registrada em log_acesso.
     */
    @GetMapping("/{cpf}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<TitularResponse> buscarPorCpf(@PathVariable String cpf, Principal principal) {
        Titular titular = titularService.buscarPorCpf(cpf, principal.getName());
        return ResponseEntity.ok(TitularResponse.from(titular));
    }

    @DeleteMapping("/{id}/consentimento")
    @PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
    public ResponseEntity<Void> revogarConsentimento(@PathVariable UUID id, Principal principal) {
        titularService.revogarConsentimento(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
