package br.com.bdregistros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro da base legal (LGPD art. 7) que autoriza o tratamento dos dados
 * de um titular para uma finalidade especifica. Sem consentimento ativo,
 * os dados do titular nao devem ser usados para a finalidade correspondente.
 */
@Entity
@Table(name = "consentimento")
public class Consentimento {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "titular_id", nullable = false)
    private Titular titular;

    @Column(nullable = false, length = 300)
    private String finalidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalConsentimento canal;

    @Column(name = "agente_responsavel", length = 150)
    private String agenteResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConsentimento status = StatusConsentimento.ATIVO;

    @Column(name = "data_consentimento", nullable = false)
    private LocalDateTime dataConsentimento;

    @Column(name = "data_revogacao")
    private LocalDateTime dataRevogacao;

    public UUID getId() {
        return id;
    }

    public Titular getTitular() {
        return titular;
    }

    public void setTitular(Titular titular) {
        this.titular = titular;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    public CanalConsentimento getCanal() {
        return canal;
    }

    public void setCanal(CanalConsentimento canal) {
        this.canal = canal;
    }

    public String getAgenteResponsavel() {
        return agenteResponsavel;
    }

    public void setAgenteResponsavel(String agenteResponsavel) {
        this.agenteResponsavel = agenteResponsavel;
    }

    public StatusConsentimento getStatus() {
        return status;
    }

    public void setStatus(StatusConsentimento status) {
        this.status = status;
    }

    public LocalDateTime getDataConsentimento() {
        return dataConsentimento;
    }

    public void setDataConsentimento(LocalDateTime dataConsentimento) {
        this.dataConsentimento = dataConsentimento;
    }

    public LocalDateTime getDataRevogacao() {
        return dataRevogacao;
    }

    public void setDataRevogacao(LocalDateTime dataRevogacao) {
        this.dataRevogacao = dataRevogacao;
    }
}
