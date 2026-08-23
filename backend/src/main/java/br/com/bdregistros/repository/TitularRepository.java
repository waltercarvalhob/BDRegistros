package br.com.bdregistros.repository;

import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TitularRepository extends JpaRepository<Titular, UUID> {

    Optional<Titular> findByCpfAndStatusNot(String cpf, StatusTitular status);

    Optional<Titular> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
