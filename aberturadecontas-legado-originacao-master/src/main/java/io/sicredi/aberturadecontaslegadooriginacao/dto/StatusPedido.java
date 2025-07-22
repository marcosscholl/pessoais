package io.sicredi.aberturadecontaslegadooriginacao.dto;

public enum StatusPedido {
    PENDING("PENDENTE"),
    CANCELLED("CANCELADO"),
    FINISHED("FINALIZADO");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
