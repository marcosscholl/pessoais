package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum TipoEndereco {
    HOME("RESIDENCIAL"),
    BUSINESS("COMERCIAL"),
    OTHER("OUTRO");

    private final String descricao;

    TipoEndereco(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
