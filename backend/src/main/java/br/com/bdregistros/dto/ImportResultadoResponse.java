package br.com.bdregistros.dto;

import java.util.List;

public class ImportResultadoResponse {

    private final int totalLinhas;
    private final int criados;
    private final int atualizados;
    private final List<ImportErroResponse> erros;

    public ImportResultadoResponse(int totalLinhas, int criados, int atualizados, List<ImportErroResponse> erros) {
        this.totalLinhas = totalLinhas;
        this.criados = criados;
        this.atualizados = atualizados;
        this.erros = erros;
    }

    public int getTotalLinhas() {
        return totalLinhas;
    }

    public int getCriados() {
        return criados;
    }

    public int getAtualizados() {
        return atualizados;
    }

    public List<ImportErroResponse> getErros() {
        return erros;
    }
}
