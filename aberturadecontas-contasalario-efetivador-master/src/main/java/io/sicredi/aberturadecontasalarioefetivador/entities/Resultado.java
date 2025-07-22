package io.sicredi.aberturadecontasalarioefetivador.entities;

import lombok.Getter;
import lombok.ToString;

import java.util.EnumSet;

@Getter
@ToString
public enum Resultado {

    RECEBIDO("Recebido"),
    EM_PROCESSAMENTO("Em Processamento"),
    CONCLUIDO("Concluido"),
    CONCLUIDO_PARCIALMENTE("Concluido Parcialmente"),
    ERRO("Erro");

    private final String descricao;

    Resultado(String descricao) {
        this.descricao = descricao;
    }

    public static Resultado map (String nome) {
        return EnumSet
                .allOf(Resultado.class)
                .stream()
                .filter(it -> it.name().equals(nome))
                .findFirst()
                .orElse(RECEBIDO);
    }

}
