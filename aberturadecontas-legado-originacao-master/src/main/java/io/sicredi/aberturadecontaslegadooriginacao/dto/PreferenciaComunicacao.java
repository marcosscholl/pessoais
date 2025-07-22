package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum PreferenciaComunicacao {
    PHONE("TELEFONE"),
    EMAIL("E-MAIL"),
    CORRESPONDENCE("CORRESPONDENCIA");

    private final String descricao;

    PreferenciaComunicacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
