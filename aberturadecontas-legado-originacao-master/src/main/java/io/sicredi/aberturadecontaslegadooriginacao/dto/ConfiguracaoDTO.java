package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConfiguracaoDTO(@JsonProperty("id") String id,
                              @JsonProperty("coopId") String idCooperativa,
                              @JsonProperty("suid") String cpf,
                              @JsonProperty("amount") Double valor,
                              @JsonProperty("scheduleDate") String diaPagamento,
                              @JsonProperty("productType") String tipoProduto) {
}