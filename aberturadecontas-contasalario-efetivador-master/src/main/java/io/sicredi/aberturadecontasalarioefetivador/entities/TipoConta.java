package io.sicredi.aberturadecontasalarioefetivador.entities;

import lombok.Getter;
import lombok.ToString;

import java.util.EnumSet;

@Getter
@ToString
public enum TipoConta {

    CONTA_CORRENTE_INDIVIDUAL("01"),
    CONTA_POUPANCA_INDIVIDUAL("02"),
    CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF("03"),
    CONTA_CORRENTE_CONJUNTA("11"),
    CONTA_POUPANCA_CONJUNTA("12");

    public final String codigo;

    TipoConta(String codigo) {
        this.codigo = codigo;
    }

    public static TipoConta map(String codigo) {
        return EnumSet
                .allOf(TipoConta.class)
                .stream()
                .filter(it -> it.getCodigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código informado no campo tpoConta inválido : " + codigo));
    }
}
