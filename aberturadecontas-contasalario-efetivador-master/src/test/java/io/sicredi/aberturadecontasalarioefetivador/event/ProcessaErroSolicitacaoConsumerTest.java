package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
import io.sicredi.aberturadecontasalarioefetivador.service.ProcessarErroCadastroContaSalarioService;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransactionDuplicatedException;
import io.sicredi.engineering.libraries.idempotent.transaction.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessaErroSolicitacaoConsumerTest {

    public static final String CABECALHO_EXTERNO_CANAL = "Canal";
    private static final String TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    public static final String TRANSACTIONID = "202409301181156156165196151";
    public static final String CANAL_EXTERNO = "EXTERNO";

    @Mock
    Acknowledgment acknowledgment;
    @Mock
    ProcessarErroCadastroContaSalarioService erroCadastroContaSalarioService;
    @Mock
    MetricService metricService;
    @InjectMocks
    ProcessaErroCadastroContaSalarioConsumer consumer;

    @Test
    @DisplayName("Deve processar evento com sucesso")
    void deveProcessarEventoComSucesso(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosComErro().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);

        IdempotentResponse<Cadastro> objectIdempotentResponse = mapIdempotenteResponse(cadastro);

        when(erroCadastroContaSalarioService.processarErroCadastroContaSalario(any(IdempotentAsyncRequest.class))).thenReturn(objectIdempotentResponse);
        doNothing().when(metricService).incrementCounter(anyString());

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verify(acknowledgment, times(1)).acknowledge();
        verify(metricService, times(1)).incrementCounter(anyString());
        verify(erroCadastroContaSalarioService, times(1)).processarErroCadastroContaSalario(any(IdempotentAsyncRequest.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao processar evento de erro")
    void naoDeveProcessarEventoComConflitoDeTransactionId(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosComErro().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        Message<String> message = MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro), messageHeaders);

        when(erroCadastroContaSalarioService.processarErroCadastroContaSalario(any(IdempotentAsyncRequest.class))).thenThrow(IdempotentTransactionDuplicatedException.class);

        Assertions.assertThrows(IdempotentTransactionDuplicatedException.class,
                () -> consumer.accept(message));

        Mockito.verifyNoInteractions(acknowledgment);
        verify(metricService, times(1)).incrementCounter(anyString());
        verify(erroCadastroContaSalarioService, times(1)).processarErroCadastroContaSalario(any(IdempotentAsyncRequest.class));

    }

    @Test
    @DisplayName("Não deve processar evento quando toggle de consumo estiver desligada")
    void naoDeveProcessarEventoQuandoToggleDeConsumoEstiverDesligada(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosComErro().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        ReflectionTestUtils.setField(consumer, "eventToggle", "false");

        doNothing().when(metricService).incrementCounter(anyString());

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verify(acknowledgment, times(1)).acknowledge();
        verify(metricService, times(1)).incrementCounter(anyString());
        verifyNoInteractions(erroCadastroContaSalarioService);
    }

    private IdempotentResponse<Cadastro> mapIdempotenteResponse(Cadastro response) {
        Map<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID_SOLICITACAO, TRANSACTIONID);
        headers.put(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO);
        return IdempotentResponse
                .<Cadastro>builder()
                .value(response)
                .errorResponse(!response.isEfetivado())
                .headers(headers)
                .build();
    }
}