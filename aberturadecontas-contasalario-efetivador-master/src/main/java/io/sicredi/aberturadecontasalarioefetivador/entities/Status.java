package io.sicredi.aberturadecontasalarioefetivador.entities;

import lombok.Getter;
import lombok.ToString;

import java.util.EnumSet;

@Getter
@ToString
public enum Status {
    PENDENTE("Pendente"),
    PROCESSANDO("Processando"),
    FINALIZADO("Finalizado");

    private final String descricao;

    Status(String descricao) {
        this.descricao = descricao;
    }

    public static Status map (String nome) {
        return EnumSet
                .allOf(Status.class)
                .stream()
                .filter(it -> it.name().equals(nome))
                .findFirst()
                .orElse(PENDENTE);
    }
}
