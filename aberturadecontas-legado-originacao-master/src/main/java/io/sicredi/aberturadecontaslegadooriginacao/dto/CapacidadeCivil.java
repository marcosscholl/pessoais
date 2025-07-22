package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum CapacidadeCivil {
    RESTRICTED("RESTRITA"),
    RELATIVE("RELATIVA"),
    FULL("TOTAL");

    private final String descricao;

    CapacidadeCivil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
