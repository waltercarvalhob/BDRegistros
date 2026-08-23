package br.com.bdregistros.dto;

public class ImportErroResponse {

    private final int linha;
    private final String mensagem;

    public ImportErroResponse(int linha, String mensagem) {
        this.linha = linha;
        this.mensagem = mensagem;
    }

    public int getLinha() {
        return linha;
    }

    public String getMensagem() {
        return mensagem;
    }
}
