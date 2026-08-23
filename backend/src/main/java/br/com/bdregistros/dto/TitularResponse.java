package br.com.bdregistros.dto;

import br.com.bdregistros.model.Endereco;
import br.com.bdregistros.model.StatusTitular;
import br.com.bdregistros.model.Titular;

import java.time.LocalDate;
import java.util.UUID;

public class TitularResponse {

    private UUID id;
    private String nomeCompleto;
    private String cpf;
    private String tituloEleitor;
    private String telefone;
    private LocalDate dataNascimento;
    private StatusTitular status;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    public static TitularResponse from(Titular titular) {
        TitularResponse response = new TitularResponse();
        response.id = titular.getId();
        response.nomeCompleto = titular.getNomeCompleto();
        response.cpf = titular.getCpf();
        response.tituloEleitor = titular.getTituloEleitor();
        response.telefone = titular.getTelefone();
        response.dataNascimento = titular.getDataNascimento();
        response.status = titular.getStatus();

        Endereco endereco = titular.getEndereco();
        if (endereco != null) {
            response.logradouro = endereco.getLogradouro();
            response.numero = endereco.getNumero();
            response.complemento = endereco.getComplemento();
            response.bairro = endereco.getBairro();
            response.cidade = endereco.getCidade();
            response.estado = endereco.getEstado();
            response.cep = endereco.getCep();
        }
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTituloEleitor() {
        return tituloEleitor;
    }

    public String getTelefone() {
        return telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public StatusTitular getStatus() {
        return status;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }

    public String getCep() {
        return cep;
    }
}
