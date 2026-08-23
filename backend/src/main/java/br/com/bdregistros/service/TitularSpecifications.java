package br.com.bdregistros.service;

import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class TitularSpecifications {

    private TitularSpecifications() {
    }

    /**
     * Sem filtro de status explicito, titulares EXCLUIDO ficam de fora por
     * padrao (mesmo criterio que ja era usado na consulta por CPF).
     */
    public static Specification<Titular> filtrar(String cpf, String nomeCompleto, String cidade, StatusTitular status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (cpf != null && !cpf.isBlank()) {
                predicate = cb.and(predicate, cb.like(root.get("cpf"), "%" + cpf + "%"));
            }
            if (nomeCompleto != null && !nomeCompleto.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get("nomeCompleto")), "%" + nomeCompleto.toLowerCase() + "%"));
            }
            if (cidade != null && !cidade.isBlank()) {
                var endereco = root.join("endereco", JoinType.LEFT);
                predicate = cb.and(predicate, cb.like(cb.lower(endereco.get("cidade")), "%" + cidade.toLowerCase() + "%"));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            } else {
                predicate = cb.and(predicate, cb.notEqual(root.get("status"), StatusTitular.EXCLUIDO));
            }
            return predicate;
        };
    }
}
