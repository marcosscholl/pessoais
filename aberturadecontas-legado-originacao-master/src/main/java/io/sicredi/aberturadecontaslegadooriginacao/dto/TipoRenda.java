package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum TipoRenda {
    DOCUMENT("COMPROVADA"),
    CUSTOMER("INFORMADA"),
    BUREAU("PRESUMIDA");

    private final String descricao;

    TipoRenda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
