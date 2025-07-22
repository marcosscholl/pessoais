package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionOrdersClient;
import io.sicredi.aberturadecontaslegadooriginacao.client.CarteiraServiceSOAPClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.ACQUISITION_ORDER;
import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.ACQUISITION_ORDER_CARTEIRA;

@Component
@Slf4j
@RequiredArgsConstructor
public class AcquisitionOrdersHandler extends AbstractHandler {

    private final AcquisitionOrdersClient acquisitionOrdersClient;
    private final CarteiraServiceSOAPClient carteiraServiceSOAPClient;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando busca do pedido no [ acquisition-orders-v1 ].", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacaoLegado = new OriginacaoLegado();
        try {
            if (!originacaoLegado.temCritica(ACQUISITION_ORDER) && originacaoLegado.acquisitionOrderProcessado()) {
                log.info("[{}] - Etapa de consulta do pedido em [ acquisition-orders-v1 ] já foi processado com sucesso.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            var acquisitionOrdersDTO = acquisitionOrdersClient.buscaPedido(originacaoLegado.getIdPedido());

            log.debug("[{}] - Pedido encontrado com sucesso no [ acquisition-orders-v1 ]. {}", originacaoLegado.getIdPedido(), acquisitionOrdersDTO);

            novaOriginacaoLegado = originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(acquisitionOrdersDTO);
            novaOriginacaoLegado.setId(originacaoLegado.getId());
            var codigoCarteira = obterCodigoCarteira(acquisitionOrdersDTO.codigoCarteira());
            if (StringUtils.isBlank(codigoCarteira)) {
                log.error("[{}] - Código da carteira [ {} ] retornado do serviço é inválido.", acquisitionOrdersDTO.codigoCarteira(), codigoCarteira);
                novaOriginacaoLegado.adicionarCritica((new Critica(ACQUISITION_ORDER_CARTEIRA, "O código da carteira retornado é inválido [ " + codigoCarteira + " ]")));
                novaOriginacaoLegado.setCodigoCarteira(acquisitionOrdersDTO.codigoCarteira());
                return novaOriginacaoLegado;
            }
            novaOriginacaoLegado.setCodigoCarteira(codigoCarteira);
            novaOriginacaoLegado.removerCritica(ACQUISITION_ORDER_CARTEIRA);

            if (novaOriginacaoLegado.acquisitionOrderProcessado()) {
                novaOriginacaoLegado.removerCritica(ACQUISITION_ORDER);
            }

            log.info("[{}] - Pedido recuperado de [ acquisition-orders-v1 ] e processado com sucesso . {}", originacaoLegado.getIdPedido(), novaOriginacaoLegado);

            return novaOriginacaoLegado;

        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar pedido no acquisition-orders-v1. error: {}", originacaoLegado.getIdPedido(), e.getMessage());
            novaOriginacaoLegado.adicionarCritica((new Critica(ACQUISITION_ORDER, "Erro ao processar acquisition-order para o pedido [ " + originacaoLegado.getIdPedido() + " ]", e.getMessage())));
            return novaOriginacaoLegado;
        }
    }

    private String obterCodigoCarteira(String idCarteira) {
        try {
            log.debug("[{}] - Iniciando consulta ao serviço carteira para obtenção do código da carteira", idCarteira);
            if(StringUtils.isBlank(idCarteira)){
                return null;
            }
            var codigoCarteira = carteiraServiceSOAPClient.obterCodigoCarteira(idCarteira).getCodigoCarteira();
            log.debug("[{}] - Finalizando consulta ao serviço carteira para obtenção do código [ {} ] da carteira com sucesso", idCarteira, codigoCarteira);
            return codigoCarteira;
        } catch (Exception e) {
            log.error("[{}] - Error ao tentar consultar o código da carteira no serviço de carteiras.", idCarteira, e);
            return null;
        }
    }
}
