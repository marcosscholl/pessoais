package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;

@Builder(toBuilder = true)
public record AcquisitionProductsCreationInputDTO(
        @JsonProperty("orderId")
        String idPedido,

        @JsonProperty("orderItemId")
        String idProdutoPedido,

        @JsonProperty("creationDate")
        LocalDate dataCriacao
) {
}
