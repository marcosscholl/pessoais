package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record AcquisitionProductsDetailsInputDTO(
        @JsonProperty("orderId")
        String idPedido,

        @JsonProperty("orderItemId")
        String idProdutoPedido,

        @JsonProperty("canBeCanceled")
        Boolean podeSerCancelado,

        @JsonProperty("status")
        Info status,

        @JsonProperty("info")
        List<Info> info,

        @JsonProperty("cancel")
        String cancelar,

        @JsonProperty("cancelInfo")
        CancelInfo infoCancelamento
) {

    @Builder(toBuilder = true)
    public record Info(
            @JsonProperty("text")
            String texto,
            @JsonProperty("appearance")
            String aparencia
    ){}

    @Builder(toBuilder = true)
    public record CancelInfo(
            @JsonProperty("cancelledBy")
            String canceladoPor,
            @JsonProperty("reason")
            String motivo,
            @JsonProperty("description")
            String descricao
    ){}

}
