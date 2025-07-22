package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum TipoTelefone {
    MOBILE("MOVEL"),
    BUSINESS("COMERCIAL"),
    HOME("RESIDENCIAL"),
    OTHER("OUTRO");

    private final String descricao;

    TipoTelefone(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
