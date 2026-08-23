package br.com.bdregistros.dto;

import br.com.bdregistros.model.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioCadastroRequest {

    @NotBlank
    @Size(max = 60)
    private String login;

    @NotBlank
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    private String senha;

    @NotBlank
    @Size(max = 150)
    private String nomeCompleto;

    @NotNull
    private Papel papel;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

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
}
