package br.com.bdregistros.dto;

import br.com.bdregistros.model.Papel;
import br.com.bdregistros.model.Usuario;

import java.util.UUID;

public class UsuarioResponse {

    private UUID id;
    private String login;
    private String nomeCompleto;
    private Papel papel;
    private boolean ativo;

    public static UsuarioResponse from(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();
        response.id = usuario.getId();
        response.login = usuario.getLogin();
        response.nomeCompleto = usuario.getNomeCompleto();
        response.papel = usuario.getPapel();
        response.ativo = usuario.isAtivo();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public Papel getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
