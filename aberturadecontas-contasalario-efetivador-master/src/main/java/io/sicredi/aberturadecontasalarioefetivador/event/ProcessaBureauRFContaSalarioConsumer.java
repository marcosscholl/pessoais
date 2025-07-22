package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.service.BureauRFService;
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

@Component("processaBureauRFContaSalarioConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaBureauRFContaSalarioConsumer implements Consumer<Message<String>> {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private final BureauRFService bureauRFService;
    private final MetricService metricService;
    @Value("${spring.cloud.stream.bindings.processaErroCadastroContaSalarioProducer-out-0.destination}")
    private final String erroCadastroTopic;
    @Value("${spring.cloud.stream.bindings.processaCadastroContaSalarioProducer-out-0.destination}")
    private final String cadastroTopic;

    @Value("${event.toggle.rfbconsumer.enabled:true}")
    private final String enabledToggle;
    @Value("${event.toggle.rfbconsumer.paused:false}")
    private final String pauseToggle;

    @Override
    public void accept(Message<String> message) {
        Cadastro cadastro = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionId = String.valueOf(message.getHeaders().get(TRANSACTION_ID));
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));

        if(TRUE.equalsIgnoreCase(pauseToggle)){
            log.info("Pausado consumo de evento : ProcessaBureauRFContaSalarioConsumer : {}", cadastro.getCpf());
            metricService.incrementCounter("event_bureaurf_pausado");
            return;
        }

        if (FALSE.equalsIgnoreCase(enabledToggle)) {
            log.info("Desabilitado consumo de evento : ProcessaBureauRFContaSalarioConsumer : {}", cadastro.getCpf());
            acknowledgment.acknowledge();
            metricService.incrementCounter("event_bureaurf_ignorado");
            return;
        }

        log.info("[{}] [{}] - Processando evento de consulta do BureauRF para cadastro de Conta Salário.",
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
                    .responseTopic(cadastroTopic)
                    .responseErrorTopic(erroCadastroTopic)
                    .build();

            bureauRFService.processarSolicitacaoBureauRF(request);
            acknowledgment.acknowledge();

            metricService.incrementCounter("event_bureaurf_sucesso");
            log.info("[{}] [{}] - Consumo do evento de consulta do BureauRF para cadastro de Conta Salário finalizado com sucesso",
                    transactionIdSolicitacao, transactionId);

        } catch (Exception e) {
            metricService.incrementCounter("event_bureaurf_erro");
            log.error("[{}] [{}] - Consumo finalizado com erro no processo do evento de consulta do BureauRF para cadastro de Conta Salário. message: {}",
                    transactionIdSolicitacao, transactionId, message, e);
            throw e;
        }
    }
}