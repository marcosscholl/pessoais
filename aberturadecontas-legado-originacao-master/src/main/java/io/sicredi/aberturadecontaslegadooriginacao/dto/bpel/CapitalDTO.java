package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Plano;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CapitalDTO(String id,
                         Double valor,
                         String diaPagamento,
                         Double planoSubscricaoValor,
                         Integer planoSubscricaoParcelas,
                         List<Plano> planos) {
}