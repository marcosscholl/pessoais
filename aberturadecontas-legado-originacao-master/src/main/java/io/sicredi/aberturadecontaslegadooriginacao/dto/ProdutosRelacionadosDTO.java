package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProdutosRelacionadosDTO(@JsonProperty("orderItemId") String idProdutoPedido,
                                      @JsonProperty("productType") String tipoProduto,
                                      @JsonProperty("status") String status) {
}