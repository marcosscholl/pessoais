package io.sicredi.aberturadecontaslegadooriginacao.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record DetalhesPedidoDTO(
        String mensagem,
        String numeroConta,
        String descricaoErro
) {
}
