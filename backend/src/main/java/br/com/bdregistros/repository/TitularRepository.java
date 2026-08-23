package br.com.bdregistros.repository;

import br.com.bdregistros.model.Titular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TitularRepository extends JpaRepository<Titular, UUID>, JpaSpecificationExecutor<Titular> {

    Optional<Titular> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
