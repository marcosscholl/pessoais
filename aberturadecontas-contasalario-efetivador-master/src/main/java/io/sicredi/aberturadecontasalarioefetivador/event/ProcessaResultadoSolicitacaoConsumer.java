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

@Component("processaResultadoSolicitacaoConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaResultadoSolicitacaoConsumer implements Consumer<Message<String>> {

    private static final String SOLICITACAO_EVENT_TYPE = "SOLICITACAO";
    private final SolicitacaoService solicitacaoService;
    @Value("${spring.cloud.stream.bindings.processaResultadoCadastroContaSalarioProducer-out-0.destination}")
    private final String resultadoCadastroTopic;
    @Value("${spring.cloud.stream.bindings.dltProcessaResultadoCadastroContaSalarioProducer-out-0.destination}")
    private final String dltResultadoCadastroTopic;
    private final MetricService metricService;
    private static final String FALSE = "false";

    @Value("${event.toggle.resultadosolicitacaoconsumer.enabled:true}")
    private final String eventToggle;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";

    @Override
    public void accept(Message<String> message) {
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));

        if (FALSE.equalsIgnoreCase(eventToggle)) {
            log.info("Desabilitado consumo evento : ProcessaResultadoSolicitacaoConsumer");
            acknowledgment.acknowledge();
            metricService.incrementCounter("event_resultadoSolicitacao_ignorado");
            return;
        }

        if (SOLICITACAO_EVENT_TYPE.equals(message.getHeaders().get("eventType"))) {
            Solicitacao solicitacaoEvent = JsonUtil.readFromJson(message.getPayload(), Solicitacao.class);
            log.info("Ignorando evento de resultado de solicitacao {}", solicitacaoEvent.getIdTransacao());
            acknowledgment.acknowledge();
            return;
        }

        Cadastro cadastroEvent = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionId = String.valueOf(message.getHeaders().get(TRANSACTION_ID));
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));

        log.info("[{}] [{}] - Processando evento de resultado de Cadastro de Conta Salário.",
                transactionIdSolicitacao, transactionId);

        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);

        try {
            IdempotentAsyncRequest<Cadastro> request = IdempotentAsyncRequest
                    .<Cadastro>builder()
                    .value(cadastroEvent)
                    .headers(headers)
                    .transactionId(transactionId.substring(0,transactionId.length()-1).concat("4"))
                    .responseTopic(resultadoCadastroTopic)
                    .responseErrorTopic(dltResultadoCadastroTopic)
                    .build();

            solicitacaoService.agregaRespostasDeCadastroDaSolicitacao(request);
            solicitacaoService.processarCallbackWebhook(transactionIdSolicitacao);
            acknowledgment.acknowledge();

            metricService.incrementCounter("event_resultadoSolicitacao_sucesso");
            log.info("[{}] [{}] - Consumo do evento de resultado de Cadastro de Conta Salário finalizado com sucesso",
                    transactionIdSolicitacao, transactionId);

        } catch (Exception e) {
            metricService.incrementCounter("event_resultadoSolicitacao_erro");
            log.error("[{}] [{}] - Consumo finalizado com erro no processo do evento de resultado de Cadastro de Conta Salário. message: {}",
                    transactionIdSolicitacao, transactionId, message, e);
            throw e;
        }
    }
}