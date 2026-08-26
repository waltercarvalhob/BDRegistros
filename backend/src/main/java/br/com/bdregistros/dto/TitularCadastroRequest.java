package br.com.bdregistros.dto;

import br.com.bdregistros.model.CanalConsentimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload preenchido pelo proprio titular (ou pelo agente, com o titular
 * presente). O consentimento e a finalidade sao obrigatorios e viram um
 * registro de Consentimento junto com o cadastro.
 */
public class TitularCadastroRequest {

    @NotBlank
    @Size(max = 150)
    private String nomeCompleto;

    @NotBlank
    private String cpf;

    private String tituloEleitor;

    private String telefone;

    private LocalDate dataNascimento;

    @NotNull
    @Valid
    private EnderecoDTO endereco;

    @NotBlank
    private String finalidadeConsentimento;

    @NotNull
    private CanalConsentimento canalConsentimento;

    private String agenteResponsavel;

    @AssertTrue(message = "E necessario confirmar o consentimento para prosseguir")
    private boolean consentimentoConfirmado;

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTituloEleitor() {
        return tituloEleitor;
    }

    public void setTituloEleitor(String tituloEleitor) {
        this.tituloEleitor = tituloEleitor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public EnderecoDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoDTO endereco) {
        this.endereco = endereco;
    }

    public String getFinalidadeConsentimento() {
        return finalidadeConsentimento;
    }

    public void setFinalidadeConsentimento(String finalidadeConsentimento) {
        this.finalidadeConsentimento = finalidadeConsentimento;
    }

    public CanalConsentimento getCanalConsentimento() {
        return canalConsentimento;
    }

    public void setCanalConsentimento(CanalConsentimento canalConsentimento) {
        this.canalConsentimento = canalConsentimento;
    }

    public String getAgenteResponsavel() {
        return agenteResponsavel;
    }

    public void setAgenteResponsavel(String agenteResponsavel) {
        this.agenteResponsavel = agenteResponsavel;
    }

    public boolean isConsentimentoConfirmado() {
        return consentimentoConfirmado;
    }

    public void setConsentimentoConfirmado(boolean consentimentoConfirmado) {
        this.consentimentoConfirmado = consentimentoConfirmado;
    }
}
