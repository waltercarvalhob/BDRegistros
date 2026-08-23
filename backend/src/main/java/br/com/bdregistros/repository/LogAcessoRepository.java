package br.com.bdregistros.repository;

import br.com.bdregistros.model.LogAcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogAcessoRepository extends JpaRepository<LogAcesso, UUID> {
}
