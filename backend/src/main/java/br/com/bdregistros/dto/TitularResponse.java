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
    private String cidade;
    private String estado;

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
            response.cidade = endereco.getCidade();
            response.estado = endereco.getEstado();
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

    public String getCidade() {
        return cidade;
    }

    public String getEstado() {
        return estado;
    }
}
