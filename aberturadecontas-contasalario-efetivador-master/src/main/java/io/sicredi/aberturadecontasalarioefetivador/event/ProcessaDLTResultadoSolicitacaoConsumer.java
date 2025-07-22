package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Component("processaDLTResultadoSolicitacaoConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaDLTResultadoSolicitacaoConsumer implements Consumer<Message<String>> {

    private static final String SOLICITACAO_EVENT_TYPE = "SOLICITACAO";
    public static final String EVENT_HEADER_RESPONSE_ERROR_TYPE = "response-error-type";
    public static final String EVENT_CONFLICT = "CONFLICT";
    private final SolicitacaoService solicitacaoService;
    @Value("${spring.cloud.stream.bindings.processaResultadoCadastroContaSalarioProducer-out-0.destination}")
    private final String resultadoCadastroTopic;
    private final MetricService metricService;
    private static final String FALSE = "false";

    @Value("${event.toggle.DLTresultadosolicitacaoconsumer.enabled:true}")
    private final String eventToggle;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";

    @Override
    public void accept(Message<String> message) {
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));
        var transactionId = message.getHeaders().get(TRANSACTION_ID);

        if (FALSE.equalsIgnoreCase(eventToggle)) {
            log.info("[{}] [{}] - Desabilitado consumo evento : ProcessaDLTResultadoSolicitacaoConsumer",
                    message.getHeaders().get("eventType"), transactionId);
            metricService.incrementCounter("dlt_event_resultadoSolicitacao_ignorado");
            return;
        }

        if (SOLICITACAO_EVENT_TYPE.equals(message.getHeaders().get("eventType"))) {
            Solicitacao solicitacaoEvent = JsonUtil.readFromJson(message.getPayload(), Solicitacao.class);
            log.info("Ignorando evento de DLT de resultado de solicitacao {}", solicitacaoEvent.getIdTransacao());
            acknowledgment.acknowledge();
            return;
        }

        if (EVENT_CONFLICT.equalsIgnoreCase(String.valueOf(message.getHeaders().get(EVENT_HEADER_RESPONSE_ERROR_TYPE)))) {
            log.info("[{}] [CONFLICT] - Consumo de evento de DLT de resultado de cadastro de Conta Salário finalizado por CONFLICT.",
                    transactionId);
            acknowledgment.acknowledge();
            metricService.incrementCounter("dlt_event_resultadoSolicitacao_conflict_bypass");
            return;
        }

        Cadastro cadastroEvent = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionIdOriginal = String.valueOf(transactionId);
        String novoTransactionId = transactionIdOriginal.substring(0,transactionIdOriginal.length()-1).concat("5");
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));

        log.info("[{}] [{}] [{}] - Processando evento de DLT de resultado de Cadastro de Conta Salário.",
                transactionIdSolicitacao, transactionIdOriginal, novoTransactionId);

        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID, transactionIdOriginal);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);

        try {
            IdempotentAsyncRequest<Cadastro> request = IdempotentAsyncRequest
                    .<Cadastro>builder()
                    .value(cadastroEvent)
                    .headers(headers)
                    .transactionId(novoTransactionId)
                    .responseTopic(resultadoCadastroTopic)
                    .build();

            solicitacaoService.agregaRespostasDeCadastroDaSolicitacao(request);
            solicitacaoService.processarCallbackWebhook(transactionIdSolicitacao);
            acknowledgment.acknowledge();

            metricService.incrementCounter("dlt_event_resultadoSolicitacao_sucesso");
            log.info("[{}] [{}] [{}] - Consumo do evento de DLT resultado de Cadastro de Conta Salário finalizado com sucesso",
                    transactionIdSolicitacao, transactionIdOriginal, novoTransactionId);

        } catch (Exception e) {
            metricService.incrementCounter("dlt_event_resultadoSolicitacao_erro");
            log.error("[{}] [{}] [{}] - Consumo finalizado com erro no processo do evento de DLT de resultado de Cadastro de Conta Salário. message: {}",
                    transactionIdSolicitacao, transactionIdOriginal, novoTransactionId, message, e);
            throw e;
        }
    }

}