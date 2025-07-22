package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum TipoParente {
    FATHER("PAI"),
    MOTHER("MAE"),
    OTHER("OUTRO");

    private final String descricao;

    TipoParente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
