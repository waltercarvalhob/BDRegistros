package br.com.bdregistros.service;

import br.com.bdregistros.dto.UsuarioAtualizacaoRequest;
import br.com.bdregistros.dto.UsuarioCadastroRequest;
import br.com.bdregistros.exception.AutoDesativacaoException;
import br.com.bdregistros.exception.LoginJaCadastradoException;
import br.com.bdregistros.exception.SenhaInvalidaException;
import br.com.bdregistros.exception.UsuarioNaoEncontradoException;
import br.com.bdregistros.model.Papel;
import br.com.bdregistros.model.Usuario;
import br.com.bdregistros.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario cadastrar(UsuarioCadastroRequest request) {
        if (usuarioRepository.existsByLogin(request.getLogin())) {
            throw new LoginJaCadastradoException("Ja existe um usuario com este login.");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(request.getLogin());
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setNomeCompleto(request.getNomeCompleto());
        usuario.setPapel(request.getPapel());
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar(String login, String nomeCompleto, Papel papel, Boolean ativo) {
        return usuarioRepository.findAll(UsuarioSpecifications.filtrar(login, nomeCompleto, papel, ativo));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario nao encontrado."));
    }

    @Transactional
    public Usuario atualizar(UUID id, UsuarioAtualizacaoRequest request) {
        Usuario usuario = buscarPorId(id);
        usuario.setNomeCompleto(request.getNomeCompleto());
        usuario.setPapel(request.getPapel());

        String novaSenha = request.getSenha();
        if (novaSenha != null && !novaSenha.isBlank()) {
            if (novaSenha.length() < 8) {
                throw new SenhaInvalidaException("A senha deve ter pelo menos 8 caracteres.");
            }
            usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        }

        return usuario;
    }

    @Transactional
    public void desativar(UUID id, String usuarioLogado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getLogin().equals(usuarioLogado)) {
            throw new AutoDesativacaoException("Nao e possivel desativar o proprio usuario logado.");
        }
        usuario.setAtivo(false);
    }
}
