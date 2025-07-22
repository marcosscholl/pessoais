package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum EstadoCivil {
    MARRIED("CASADO"),
    DIVORCED("DIVORCIADO"),
    SEPARATED("SEPARADO JUDICIALMENTE"),
    SINGLE("SOLTEIRO"),
    WIDOWED("VIUVO");

    private final String descricao;

    EstadoCivil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
