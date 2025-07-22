package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum Papel {
    HOLDER("TITULAR"),
    UPHOLDER("REPRESENTANTE"),
    UNDERAGE_HOLDER("TITULAR_MENOR"),
    UNDERAGE_UPHOLDER("REPRESENTANTE_MENOR");

    private final String descricao;

    Papel(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
