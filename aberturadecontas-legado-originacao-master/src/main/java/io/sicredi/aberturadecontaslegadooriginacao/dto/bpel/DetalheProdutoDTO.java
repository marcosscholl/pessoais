package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetalheProdutoDTO(String idItemPedido,
                                String idSimulacao,
                                String idCatalogoProduto,
                                String tipoProduto,
                                String codigoProduto,
                                String marca,
                                String status,
                                String numeroConta,
                                List<DadosRelacionamentoDTO> relacionamento,
                                String mensagemDetalhe,
                                ConfiguracaoDetalheDTO configuracao) {
}