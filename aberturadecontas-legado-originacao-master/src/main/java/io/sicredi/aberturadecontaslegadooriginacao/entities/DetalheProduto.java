package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DetalheProduto {

    private String idItemPedido;
    private String idSimulacao;
    private String idCatalogoProduto;
    private String tipoProduto;
    private String codigoProduto;
    private String marca;
    private String status;
    private String numeroConta;
    private List<DadosRelacionamento> relacionamento;
    private String mensagemDetalhe;
    private ConfiguracaoDetalhe configuracao;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}