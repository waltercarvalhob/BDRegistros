package br.com.bdregistros.controller;

import br.com.bdregistros.dto.UsuarioAtualizacaoRequest;
import br.com.bdregistros.dto.UsuarioCadastroRequest;
import br.com.bdregistros.dto.UsuarioResponse;
import br.com.bdregistros.model.Papel;
import br.com.bdregistros.model.Usuario;
import br.com.bdregistros.service.UsuarioService;
import jakarta.validation.Valid;
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
import java.util.List;
import java.util.UUID;

/**
 * Somente ADMIN gerencia usuarios internos (criar, listar, editar,
 * desativar). Nao ha autocadastro de OPERADOR: quem pode consultar CPF
 * alheio precisa ser habilitado por alguem que ja e administrador do
 * sistema.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioCadastroRequest request) {
        Usuario usuario = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(@RequestParam(required = false) String login,
                                                          @RequestParam(required = false) String nomeCompleto,
                                                          @RequestParam(required = false) Papel papel,
                                                          @RequestParam(required = false) Boolean ativo) {
        List<UsuarioResponse> resultado = usuarioService.listar(login, nomeCompleto, papel, ativo).stream()
                .map(UsuarioResponse::from)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obter(@PathVariable UUID id) {
        return ResponseEntity.ok(UsuarioResponse.from(usuarioService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(@PathVariable UUID id,
                                                       @Valid @RequestBody UsuarioAtualizacaoRequest request) {
        Usuario usuario = usuarioService.atualizar(id, request);
        return ResponseEntity.ok(UsuarioResponse.from(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id, Principal principal) {
        usuarioService.desativar(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
