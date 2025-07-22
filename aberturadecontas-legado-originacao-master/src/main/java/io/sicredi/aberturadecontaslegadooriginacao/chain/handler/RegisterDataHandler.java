package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionRegisterDataClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.REGISTER_DATA;

@Component
@Slf4j
@RequiredArgsConstructor
public class RegisterDataHandler extends AbstractHandler {

    private final AcquisitionRegisterDataClient acquisitionRegisterDataClient;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando busca do pedido no register-data.", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacaoLegado = new OriginacaoLegado();
        try {

            if (!originacaoLegado.temCritica(REGISTER_DATA) && originacaoLegado.registerDataProcessado()) {
                log.info("[{}] - Busca dos documentos no pedido no register-data processado com sucesso.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            var documentosDoPedido = acquisitionRegisterDataClient.buscarDocumentosDoPedido(originacaoLegado.getIdPedido());

            originacaoLegadoMapper.mapListaRegisterDataDTOParaOriginacaoLegado(documentosDoPedido, originacaoLegado);

            log.debug("[{}] - Documentos do pedido recuperados e processados com sucesso em acquisition-register-data. {}", originacaoLegado.getIdPedido(), documentosDoPedido);

            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);

            if(novaOriginacaoLegado.registerDataProcessado()){
                novaOriginacaoLegado.removerCritica(REGISTER_DATA);
            }

            log.info("[{}] - Busca do pedido no register-data finalizado com sucesso.", originacaoLegado.getIdPedido());

            return novaOriginacaoLegado;

        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar os documentos do pedido no [ acquisition-register-data ]. error: {}", originacaoLegado.getIdPedido(), e.getMessage());
            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.adicionarCritica(new Critica(REGISTER_DATA, "Erro ao processar os dados do [ acquisition-register-data ] para o pedido [ " + originacaoLegado.getIdPedido() + " ]", e.getMessage()));
            return novaOriginacaoLegado;
        }
    }
}
