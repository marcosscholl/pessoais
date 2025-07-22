package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PedidoDTO(@JsonProperty("offerId") String idOferta,
                        @JsonProperty("organization") String cooperativa,
                        @JsonProperty("agency") String agencia,
                        @JsonProperty("enrollmentType") String tipoInscricao,
                        @JsonProperty("itemsRelated") List<ProdutosRelacionadosDTO> produtosRelacionados,
                        @JsonProperty("originSource") String canal) {
}