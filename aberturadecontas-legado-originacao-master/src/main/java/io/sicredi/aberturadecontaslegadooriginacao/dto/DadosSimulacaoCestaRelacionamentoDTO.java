package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosSimulacaoCestaRelacionamentoDTO(@JsonProperty("paymentDay") Integer diaPagamento,
                                                   @JsonProperty("coreId") String id) {
}
