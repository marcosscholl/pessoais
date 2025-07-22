package io.sicredi.aberturadecontasalarioefetivador.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.EnumSet;

@Getter
@ToString
public enum ContaSalarioEventoDestinoCredito {

    SAQUE("A", "MODALIDADE_SAQUE"),
    CONTA_CORRENTE_SICREDI("B", "PORTABILIDADE_CONTA_CORRENTE_SICREDI"),
    OUTRA_IF("C", "PORTABILIDADE_OUTRA_INSTITUICAO"),
    CONTA_POUPANCA_SICREDI("D", "PORTABILIDADE_CONTA_POUPANCA_SICREDI"),
    CONTA_DIGITAL_SICREDI("W", "PORTABILIDADE_CONTA_DIGITAL_SICREDI"),
    OUTRO("99", "OUTRO");;

    public final String codigo;
    public final String descricao;

    ContaSalarioEventoDestinoCredito(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static ContaSalarioEventoDestinoCredito map(String codigo) {
        return EnumSet
                .allOf(ContaSalarioEventoDestinoCredito.class)
                .stream()
                .filter(it -> it.getCodigo().equals(codigo))
                .findFirst()
                .orElse(OUTRO);
    }
}