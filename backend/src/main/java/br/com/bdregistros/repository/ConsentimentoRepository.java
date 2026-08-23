package br.com.bdregistros.repository;

import br.com.bdregistros.model.Consentimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsentimentoRepository extends JpaRepository<Consentimento, UUID> {

    List<Consentimento> findByTitularId(UUID titularId);
}
