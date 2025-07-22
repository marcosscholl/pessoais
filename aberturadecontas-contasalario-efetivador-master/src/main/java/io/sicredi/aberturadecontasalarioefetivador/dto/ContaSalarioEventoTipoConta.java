package io.sicredi.aberturadecontasalarioefetivador.dto;

import lombok.Getter;
import lombok.ToString;

import java.util.EnumSet;

@Getter
@ToString
public enum ContaSalarioEventoTipoConta {

    CONTA_CORRENTE_INDIVIDUAL("01", "CONTA_CORRENTE_INDIVIDUAL"),
    CONTA_POUPANCA_INDIVIDUAL("02", "CONTA_POUPANCA_INDIVIDUAL"),
    CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF("03", "CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF"),
    CONTA_CORRENTE_CONJUNTA("11", "CONTA_CORRENTE_CONJUNTA"),
    CONTA_POUPANCA_CONJUNTA("12", "CONTA_POUPANCA_CONJUNTA"),
    CONTA_PAGAMENTO("99", "CONTA_PAGAMENTO");

    public final String codigo;
    public final String descricao;

    ContaSalarioEventoTipoConta(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public static ContaSalarioEventoTipoConta map(String codigo) {
        return EnumSet
                .allOf(ContaSalarioEventoTipoConta.class)
                .stream()
                .filter(it -> it.getCodigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código informado inválido : " + codigo));
    }
}