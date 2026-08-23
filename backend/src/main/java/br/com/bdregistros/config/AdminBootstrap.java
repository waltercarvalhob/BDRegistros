package br.com.bdregistros.config;

import br.com.bdregistros.model.Papel;
import br.com.bdregistros.model.Usuario;
import br.com.bdregistros.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro usuario ADMIN a partir de variaveis de ambiente, apenas
 * se ainda nao existir nenhum usuario. Nao ha credencial padrao embutida:
 * sem ADMIN_LOGIN/ADMIN_PASSWORD definidos, o sistema so avisa no log e
 * segue sem criar nada (o primeiro usuario precisa entrar direto no banco).
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_LOGIN:}")
    private String adminLogin;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public AdminBootstrap(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        if (adminLogin.isBlank() || adminPassword.isBlank()) {
            log.warn("Nenhum usuario cadastrado. Defina ADMIN_LOGIN e ADMIN_PASSWORD como variaveis de "
                    + "ambiente para criar o primeiro administrador automaticamente.");
            return;
        }

        if (adminPassword.length() < 8) {
            log.error("ADMIN_PASSWORD tem menos de 8 caracteres; usuario administrador nao foi criado.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setLogin(adminLogin);
        admin.setSenhaHash(passwordEncoder.encode(adminPassword));
        admin.setNomeCompleto("Administrador");
        admin.setPapel(Papel.ADMIN);
        admin.setAtivo(true);
        usuarioRepository.save(admin);

        log.info("Usuario administrador '{}' criado a partir das variaveis de ambiente.", adminLogin);
    }
}
