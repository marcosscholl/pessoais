package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
import io.sicredi.aberturadecontasalarioefetivador.service.ProcessarErroCadastroContaSalarioService;
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

@Component("processaErroCadastroContaSalarioConsumer")
@Slf4j
@AllArgsConstructor
public class ProcessaErroCadastroContaSalarioConsumer implements Consumer<Message<String>> {

    private final ProcessarErroCadastroContaSalarioService erroCadastroContaSalarioService;
    private final MetricService metricService;
    @Value("${spring.cloud.stream.bindings.processaResultadoCadastroContaSalarioProducer-out-0.destination}")
    private final String resultadoCadastroTopic;
    @Value("${spring.cloud.stream.bindings.dltProcessaErroCadastroContaSalarioProducer-out-0.destination}")
    private final String dltErroCadastroTopic;
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String FALSE = "false";

    @Value("${event.toggle.cadastroerroconsumer.enabled:true}")
    private final String eventToggle;

    @Override
    public void accept(Message<String> message) {
        Cadastro cadastro = JsonUtil.readFromJson(message.getPayload(), Cadastro.class);
        String transactionId = String.valueOf(message.getHeaders().get(TRANSACTION_ID));
        String transactionIdSolicitacao = String.valueOf(message.getHeaders().get(TRANSACTION_ID_SOLICITACAO));
        Acknowledgment acknowledgment = Objects.requireNonNull(message.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT,
                Acknowledgment.class));

        if (FALSE.equalsIgnoreCase(eventToggle)) {
            log.info("Desabilitado consumo evento : ProcessaErroCadastroContaSalarioConsumer : {}", cadastro.getCpf());
            acknowledgment.acknowledge();
            metricService.incrementCounter("event_erroCadastro_ignorado");
            return;
        }

        log.info("[{}] [{}] - Processando evento de erro de Cadastro de Conta Salário.",
                transactionIdSolicitacao, transactionId);

        try {
            IdempotentAsyncRequest<Cadastro> request = mapIdempotentAsyncRequest(cadastro, transactionId, transactionIdSolicitacao);

            log.info("[{}][{}] - Iniciando processamento de evento de erro no cadastro.", transactionIdSolicitacao, transactionId);

            erroCadastroContaSalarioService.processarErroCadastroContaSalario(request);
            acknowledgment.acknowledge();

            metricService.incrementCounter("event_erroCadastro_sucesso");
            log.info("[{}] [{}] - Consumo do evento de erro de Cadastro de Conta Salário finalizado com sucesso",
                    transactionIdSolicitacao, transactionId);
        } catch (Exception e) {
            metricService.incrementCounter("event_erroCadastro_erro");
            log.error("[{}] [{}] - Consumo finalizado com erro no processo do evento de erro de Cadastro de Conta Salário. message: {}",
                    transactionIdSolicitacao, transactionId, message, e);
            throw e;
        }

    }

    private IdempotentAsyncRequest<Cadastro> mapIdempotentAsyncRequest(Cadastro cadastro, String transactionId, String transactionIdSolicitacao) {
        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID, transactionId);
        headers.put(TRANSACTION_ID_SOLICITACAO, transactionIdSolicitacao);

        return IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(headers)
                .transactionId(transactionId.substring(0, transactionId.length() - 1).concat("3"))
                .responseTopic(resultadoCadastroTopic)
                .responseErrorTopic(dltErroCadastroTopic)
                .build();
    }

}