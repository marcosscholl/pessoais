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

@Component("processaDLTErroCadastroContaSalarioConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaDLTErroCadastroContaSalarioConsumer implements Consumer<Message<String>> {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String FALSE = "false";
    public static final String EVENT_HEADER_RESPONSE_ERROR_TYPE = "response-error-type";
    public static final String EVENT_CONFLICT = "CONFLICT";
    private final DltEventService dltEventService;
    private final MetricService metricService;
    @Value("${spring.cloud.stream.bindings.processaErroCadastroContaSalarioProducer-out-0.destination}")
    private final String erroCadastroTopic;

    @Value("${event.toggle.DLTErroCadastrosConsumer.enabled:true}")
    private final String eventToggle;

    @Override
    public void accept(Message<String> message) {
        Cadastro cadastro = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionId = String.valueOf(message.getHeaders().get(TRANSACTION_ID));
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));

        if (FALSE.equalsIgnoreCase(eventToggle)) {
            log.info("Desabilitado consumo evento : ProcessaDLTErroCadastroContaSalarioConsumer : {}", cadastro.getCpf());
            acknowledgment.acknowledge();
            metricService.incrementCounter("dlt_event_erroCadastro_ignorado");
            return;
        }

        if (EVENT_CONFLICT.equalsIgnoreCase(String.valueOf(message.getHeaders().get(EVENT_HEADER_RESPONSE_ERROR_TYPE)))) {
            log.info("[{}] [{}] [CONFLICT] - Consumo de evento de DLT de erro de cadastro de Conta Salário finalizado por CONFLICT.",
                    transactionIdSolicitacao, transactionId);
            acknowledgment.acknowledge();
            metricService.incrementCounter("dlt_event_erroCadastro_conflict_bypass");
            return;
        }

        log.info("[{}] [{}] - Processando evento de DLT de erro de cadastro de Conta Salário.",
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

            dltEventService.processarErroSolicitacaoCadastro(request);
            acknowledgment.acknowledge();

            metricService.incrementCounter("dlt_event_erroCadastro_sucesso");
            log.info("[{}] [{}] - Consumo de evento de DLT de erro de cadastro de Conta Salário finalizado.",
                    transactionIdSolicitacao, transactionId);

        } catch (Exception e) {
            metricService.incrementCounter("dlt_event_erroCadastro_erro");
            log.error("[{}] [{}] - Consumo de evento de DLT de erro de cadastro de Conta Salário finalizado com erro. message: {}",
                    transactionIdSolicitacao, transactionId, message, e);
            throw e;
        }
    }
}