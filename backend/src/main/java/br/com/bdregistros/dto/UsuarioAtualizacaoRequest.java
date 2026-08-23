package br.com.bdregistros.dto;

import br.com.bdregistros.model.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// senha e opcional: em branco/ausente mantem a senha atual. A validacao de
// tamanho minimo (quando preenchida) e feita manualmente em UsuarioService,
// ja que @Size trataria "" como invalida em vez de "sem alteracao".

public class UsuarioAtualizacaoRequest {

    @NotBlank
    @Size(max = 150)
    private String nomeCompleto;

    @NotNull
    private Papel papel;

    private String senha;

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
