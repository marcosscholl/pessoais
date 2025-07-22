package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.GestentConectorClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.GESTENT;

@Component
@Slf4j
@RequiredArgsConstructor
public class GestentHandler extends AbstractHandler {

    private final GestentConectorClient gestentConectorClient;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando busca do código da entidade no gestent-conector-api.", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacaoLegado = new OriginacaoLegado();
        var cooperativa = event.pedido() != null ? event.pedido().cooperativa() : originacaoLegado.getCooperativa();
        var agencia = event.pedido() != null ? event.pedido().agencia() : originacaoLegado.getAgencia();
        try {

            if (!originacaoLegado.temCritica(GESTENT) && originacaoLegado.gestentProcessado()) {
                log.info("[{}] - Código da entidade já processado com sucesso.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            var codigoEntidade = gestentConectorClient.buscarCodigoEntidade(cooperativa, agencia);

            log.debug("[{}] - Código da entidade encontrado com sucesso no gestent-conector-api. Cooperativa: {}, Agência: {}", codigoEntidade, cooperativa, agencia);

            var entidade = Optional.ofNullable(codigoEntidade)
                    .orElseThrow(NotFoundException::new)
                    .codigoEntidade()
                    .stream()
                    .findFirst()
                    .orElseThrow(NotFoundException::new);

            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.setCodigoEntidade(entidade.codigoEntidade());
            novaOriginacaoLegado.setSiglaEstado(entidade.siglaEstado());
            novaOriginacaoLegado.setNomeCidade(entidade.nomeCidade());

            if(novaOriginacaoLegado.gestentProcessado()){
                novaOriginacaoLegado.removerCritica(GESTENT);
            }

            log.info("[{}] - Codigo da entidade recuperado e processado com sucesso. {}", originacaoLegado.getIdPedido(), originacaoLegado);

            return novaOriginacaoLegado;

        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar código da entidade no serviço [ gestent-conector-api ]. error: {}", originacaoLegado.getIdPedido(), e.getMessage());
            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.adicionarCritica(new Critica(GESTENT, "Erro ao processar os dados do código do pedido em [ gestent-conector-api ] para o pedido [ " + originacaoLegado.getIdPedido() + " ]", e.getMessage()));
            return novaOriginacaoLegado;
        }
    }
}
