package br.com.bdregistros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Trilha de auditoria exigida pela LGPD (art. 37) para demonstrar quem
 * acessou o dado pessoal de um titular e quando.
 */
@Entity
@Table(name = "log_acesso")
public class LogAcesso {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "titular_id", nullable = false)
    private UUID titularId;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 30)
    private String acao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    public UUID getId() {
        return id;
    }

    public UUID getTitularId() {
        return titularId;
    }

    public void setTitularId(UUID titularId) {
        this.titularId = titularId;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
