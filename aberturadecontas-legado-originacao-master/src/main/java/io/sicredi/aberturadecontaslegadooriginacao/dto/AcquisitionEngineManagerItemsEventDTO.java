package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AcquisitionEngineManagerItemsEventDTO(@JsonProperty("id") String id,
                                                    @JsonProperty("orderId") String idPedido,
                                                    @JsonProperty("order") PedidoDTO pedido,
                                                    @JsonProperty("item") ProdutoDTO item) {
}