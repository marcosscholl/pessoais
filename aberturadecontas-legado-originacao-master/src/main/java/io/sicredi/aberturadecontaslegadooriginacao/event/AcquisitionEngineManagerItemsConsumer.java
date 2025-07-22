package io.sicredi.aberturadecontaslegadooriginacao.event;

import io.sicredi.aberturadecontaslegadooriginacao.chain.OriginacaoFisitalLegadoChain;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import io.sicredi.aberturadecontaslegadooriginacao.service.MetricasService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Component("acquisitionEngineManagerItemsConsumer")
public class AcquisitionEngineManagerItemsConsumer implements Consumer<Message<String>> {

    private final MetricasService metricasService;
    private final OriginacaoFisitalLegadoChain originacaoFisitalLegadoChain;
    private final OriginacaoLegadoRepository originacaoLegadoRepository;
    private static final String COD_CONTA_CAPITAL_LEGADO = "CSOC";
    private static final String COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO = "INVST";
    private static final String NOME_DA_METRICA = "event_acquisition_engine_manager_items";
    private static final String STATUS_PRODUTO_INICIADO = "STARTED";
    private static final String TAG_RESULTADO = "resultado";

    @Value("${sicredi.aberturadecontas-legado-originacao.kafka.acquisitionEngineManagerItemsConsumerHabilitado}")
    private final boolean acquisitionEngineManagerItemsConsumerHabilitado;

    @Override
    public void accept(Message<String> message) {
        if (!acquisitionEngineManagerItemsConsumerHabilitado) {
            log.info("Consumer do tópico [acquisition-engine-manager-items-v1] está desabilitado.");
            return;
        }

        var acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));
        var acquisitionEngineManagerItemsDTO = JsonUtils.jsonParaObjeto(message.getPayload(), AcquisitionEngineManagerItemsEventDTO.class);

        try{
            if(deveProcessarEvento(acquisitionEngineManagerItemsDTO)) {

                log.info("Processando evento acquisitionEngineManagerItems. {}", acquisitionEngineManagerItemsDTO);

                originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(acquisitionEngineManagerItemsDTO);

                metricasService.incrementCounter(NOME_DA_METRICA,TAG_RESULTADO, "sucesso");
                log.info("Evento acquisitionEngineManagerItems processado. {}", acquisitionEngineManagerItemsDTO);
            }
            else{
                log.debug("Consumo de evento ignorado. {}", message);
                metricasService.incrementCounter(NOME_DA_METRICA,TAG_RESULTADO, "ignorado");
            }
        }catch (Exception e){
            log.error("Erro ao realizar consumo de evento - AcquisitionEngineManagerItemsConsumer: {}", e.getMessage(), e);
            metricasService.incrementCounter(NOME_DA_METRICA,TAG_RESULTADO, "erro");
            throw e;
        }
        acknowledgment.acknowledge();
    }

    private boolean deveProcessarEvento(AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemsDTO) {
        return isCapitalLegacyStarted(acquisitionEngineManagerItemsDTO) ||
                isInvestmentLegacyStartedUnico(acquisitionEngineManagerItemsDTO) &&
                naoExisteProcessoInternoParaPedido(acquisitionEngineManagerItemsDTO.idPedido());
    }

    private boolean naoExisteProcessoInternoParaPedido(String idPedido) {
        return originacaoLegadoRepository.findByIdPedido(idPedido)
                .map(pedidoEncontrado -> {
                    log.info("Existe processo interno para idPedido: {} com status: {}. Deverá ser ignorado.", pedidoEncontrado.getIdPedido(), pedidoEncontrado.getStatus());
                    return Boolean.FALSE;
                })
                .orElseGet(() -> Boolean.TRUE);
    }

    private boolean isCapitalLegacyStarted(AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemsDTO) {
        return COD_CONTA_CAPITAL_LEGADO.equalsIgnoreCase(acquisitionEngineManagerItemsDTO.item().codigoProduto()) &&
                STATUS_PRODUTO_INICIADO.equalsIgnoreCase(acquisitionEngineManagerItemsDTO.item().status().name());
    }

    private boolean isInvestmentLegacyStartedUnico(AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemsDTO) {
        return COD_CONTA_POUPANCA_INDIVIDUAL_LEGADO.equalsIgnoreCase(acquisitionEngineManagerItemsDTO.item().codigoProduto()) &&
                STATUS_PRODUTO_INICIADO.equalsIgnoreCase(acquisitionEngineManagerItemsDTO.item().status().name()) &&
                CollectionUtils.isEmpty(acquisitionEngineManagerItemsDTO.pedido().produtosRelacionados());
    }
}