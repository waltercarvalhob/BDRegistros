package br.com.bdregistros.service;

import br.com.bdregistros.model.Papel;
import br.com.bdregistros.model.Usuario;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    public static Specification<Usuario> filtrar(String login, String nomeCompleto, Papel papel, Boolean ativo) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (login != null && !login.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("login")), "%" + login.toLowerCase() + "%"));
            }
            if (nomeCompleto != null && !nomeCompleto.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("nomeCompleto")), "%" + nomeCompleto.toLowerCase() + "%"));
            }
            if (papel != null) {
                predicate = cb.and(predicate, cb.equal(root.get("papel"), papel));
            }
            if (ativo != null) {
                predicate = cb.and(predicate, cb.equal(root.get("ativo"), ativo));
            }
            return predicate;
        };
    }
}
