package br.com.bdregistros.security;

import br.com.bdregistros.model.Usuario;
import br.com.bdregistros.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class BdRegistrosUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public BdRegistrosUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado ou inativo."));

        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getPapel().name()))
                .build();
    }
}
