package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum StatusDocumento {
    PENDING("PENDENTE"),
    APPROVED("APROVADO"),
    REPROVED("REPROVADO"),
    CANCELLED("CANCELADO");

    private final String descricao;

    StatusDocumento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
