package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum Genero {
    FEMALE("FEMININO"),
    MALE("MASCULINO"),
    OTHER("OUTRO");

    private final String descricao;

    Genero(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
