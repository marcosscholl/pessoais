package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Capital {

    private String id;
    private Double valor;
    private LocalDate diaPagamento;
    private Double planoSubscricaoValor;
    private Integer planoSubscricaoParcelas;
    private List<Plano> planos;

    public Capital(Double valor, LocalDate diaPagamento){
        this.valor = valor;
        this.diaPagamento = diaPagamento;
    }

    public Capital(List<Plano> planos){
        this.planos = planos;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}