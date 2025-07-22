package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.OriginacaoLegadoClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.PedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoDTOMapper;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class OriginacaoFisitalLegadoHandler extends AbstractHandler {

    private final OriginacaoLegadoRepository originacaoLegadoRepository;
    private final OriginacaoLegadoDTOMapper originacaoLegadoDTOMapper;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;
    private final OriginacaoLegadoClient originacaoLegadoClient;

    @Override
    public OriginacaoLegado processarProximo(AcquisitionEngineManagerItemsEventDTO event, OriginacaoLegado originacaoLegado) {
        log.info("Processando originação fisital-legado. {}", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacaoLegado = new OriginacaoLegado();
        try {

            if (originacaoLegado.originacaoFisitalLegadoProcessado()) {
                log.info("[{}] - Originação fisital-legado já foi enviada ao BPL. {}", originacaoLegado.getIdPedido(), originacaoLegado);
                return originacaoLegado;
            }

            novaOriginacaoLegado = originacaoLegadoMapper.merge(originacaoLegado);

            if (!originacaoLegado.isCompleto()) {
                log.warn("[{}] - Originação fisital-legado não enviada ao BPEL pois não está pronto para efetivação. {}", originacaoLegado.getIdPedido(), originacaoLegado);
                novaOriginacaoLegado.adicionarCritica(new Critica(ORIGINACAO_LEGADO, "Originação fisital-legado não enviada ao BPEL pois não está pronto para efetivação. Id pedido [ " + originacaoLegado.getIdPedido() + " ]"));
                salvarOriginacao(novaOriginacaoLegado);
                return novaOriginacaoLegado;
            }

            novaOriginacaoLegado.removerCritica(ORIGINACAO_LEGADO);
            salvarOriginacao(novaOriginacaoLegado);

            log.info("[{}] - Enviando originação para o BPEL.", originacaoLegado.getIdPedido());
            originacaoLegadoClient.processaOriginacao(new PedidoDTO(originacaoLegado.getIdPedido()));

            log.info("[{}] - Originação fisital-legado processada com sucesso. {}", originacaoLegado.getIdPedido(), originacaoLegado);
            return novaOriginacaoLegado;
        } catch (Exception ex) {
            log.error("[{}] - Erro processar originação fisital-legado. error: {}", originacaoLegado.getIdPedido(), ex.getMessage(), ex);
            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacaoLegado);
            novaOriginacaoLegado.adicionarCritica(new Critica(ORIGINACAO_LEGADO, "Erro processar originação fisital-legado para o pedido [ " + originacaoLegado.getIdPedido() + " ]", ex.getMessage()));
            salvarOriginacao(novaOriginacaoLegado);
            return novaOriginacaoLegado;
        }
    }

    private void salvarOriginacao(OriginacaoLegado originacaoLegado) {
        originacaoLegado.setDataAtualizacao(LocalDateTime.now());
        originacaoLegadoRepository.save(originacaoLegado);
    }

    public OriginacaoLegadoDTO buscarOriginacaoLegadoPorIdPedido(String idPedido) {
        try {
            var originacaoLegado = originacaoLegadoRepository.findByIdPedido(idPedido).orElseThrow(NotFoundException::new);
            return originacaoLegadoDTOMapper.map(originacaoLegado);
        } catch (Exception ex) {
            log.error("Erro ao buscar os dados de originação legado.", ex);
            return null;
        }
    }

    public String buscarDadosEntidadeOriginacaoLegadoPorIdPedido(String idPedido) {
        try {
            var originacao = originacaoLegadoRepository.findByIdPedido(idPedido).orElseThrow(NotFoundException::new);
            return JsonUtils.objetoParaJson(originacao);
        } catch (Exception ex) {
            log.error("Erro ao buscar os dados de originação legado.", ex);
            return null;
        }
    }
}
