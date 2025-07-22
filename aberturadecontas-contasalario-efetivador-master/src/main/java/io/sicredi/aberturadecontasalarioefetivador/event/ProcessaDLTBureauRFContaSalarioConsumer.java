package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.service.DltEventService;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
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

@Component("processaDLTBureauRFContaSalarioConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaDLTBureauRFContaSalarioConsumer implements Consumer<Message<String>> {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    public static final String EVENT_HEADER_RESPONSE_ERROR_TYPE = "response-error-type";
    public static final String EVENT_CONFLICT = "CONFLICT";
    private static final String FALSE = "false";
    private final DltEventService dltEventS;
    private final MetricService metricService;
    @Value("${spring.cloud.stream.bindings.processaErroCadastroContaSalarioProducer-out-0.destination}")
    private final String erroCadastroTopic;

    @Value("${event.toggle.DLTBureauRFConsumer.enabled:true}")
    private final String eventToggle;

    @Override
    public void accept(Message<String> message) {
        Cadastro cadastro = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionId = String.valueOf(message.getHeaders().get(TRANSACTION_ID));
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));

        if (FALSE.equalsIgnoreCase(eventToggle)) {
            log.info("Desabilitado consumo evento : ProcessaDLTBureauRFContaSalarioConsumer : {}", cadastro.getCpf());
            acknowledgment.acknowledge();
            metricService.incrementCounter("dlt_event_bureaurf_ignorado");
            return;
        }

        if (EVENT_CONFLICT.equalsIgnoreCase(String.valueOf(message.getHeaders().get(EVENT_HEADER_RESPONSE_ERROR_TYPE)))) {
            log.info("[{}] [{}] [CONFLICT] - Consumo de evento de DLT de BureauRF finalizado por CONFLICT.",
                    transactionIdSolicitacao, transactionId);
            acknowledgment.acknowledge();
            metricService.incrementCounter("dlt_event_bureaurf_conflict_bypass");
            return;
        }

        log.info("[{}] [{}] - Processando evento de Erro de consulta do BureauRF para cadastro de Conta Salário.",
                transactionIdSolicitacao, transactionId);

        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);
        try {
            IdempotentAsyncRequest<Cadastro> request = IdempotentAsyncRequest
                    .<Cadastro>builder()
                    .value(cadastro)
                    .headers(headers)
                    .transactionId(transactionId)
                    .responseTopic(erroCadastroTopic)
                    .responseErrorTopic(erroCadastroTopic)
                    .build();

            dltEventS.processarErroSolicitacaoBureauRF(request);
            acknowledgment.acknowledge();

            metricService.incrementCounter("dlt_event_bureaurf_sucesso");
            log.info("[{}] [{}] - Consumo finalizado no processo do evento de Erro de consulta do BureauRF para cadastro de Conta Salário.",
                    transactionIdSolicitacao, transactionId);

        } catch (Exception e) {
            metricService.incrementCounter("dlt_event_bureaurf_erro");
            log.error("[{}] [{}] - Consumo finalizado com erro no processo do evento de consulta do BureauRF para cadastro de Conta Salário. message: {}",
                    transactionIdSolicitacao, transactionId, message, e);
            throw e;
        }
    }
}