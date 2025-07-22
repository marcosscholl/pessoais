package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProdutoDTO(@JsonProperty("id") String id,
                         @JsonProperty("orderItemId") String idProdutoPedido,
                         @JsonProperty("simulationId") String idSimulacao,
                         @JsonProperty("productCatalogId") String idCatalogoProduto,
                         @JsonProperty("productType") String tipoProduto,
                         @JsonProperty("productCode") String codigoProduto,
                         @JsonProperty("brand") String marca,
                         @JsonProperty("status") StatusProduto status,
                         @JsonProperty("needsToBeMembership") Boolean precisaDeAdesao,
                         @JsonProperty("customers") List<CadastroDTO> cadastros) {
}