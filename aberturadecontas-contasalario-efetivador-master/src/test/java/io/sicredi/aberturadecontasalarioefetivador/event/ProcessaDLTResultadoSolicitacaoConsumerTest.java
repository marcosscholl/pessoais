package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
import io.sicredi.aberturadecontasalarioefetivador.service.SolicitacaoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessaDLTResultadoSolicitacaoConsumerTest {
    @Mock
    Acknowledgment acknowledgment;
    @Mock
    SolicitacaoService solicitacaoService;
    @Mock
    MetricService metricService;
    @InjectMocks
    ProcessaDLTResultadoSolicitacaoConsumer consumer;

    @Test
    @DisplayName("Deve processar evento de resultado de cadastro com sucesso")
    void deveProcessarEventoComSucesso(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        doNothing().when(metricService).incrementCounter(anyString());
        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verify(solicitacaoService, times(1)).agregaRespostasDeCadastroDaSolicitacao(any());
        verify(solicitacaoService, times(1)).processarCallbackWebhook(any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Nao deve processar evento de resultado de Solicitacao")
    void naoDeveProcessarEventoDeSolicitacao(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        header.put("eventType", "SOLICITACAO");
        MessageHeaders messageHeaders = new MessageHeaders(header);

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verifyNoInteractions(solicitacaoService);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção quando ocorrer erro ao processar evento de resultado de cadastro")
    void naoDeveProcessarEventoComConflitoDeTransactionId(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        Message<String> message = MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro), messageHeaders);
        doNothing().when(metricService).incrementCounter(anyString());
        when(solicitacaoService.agregaRespostasDeCadastroDaSolicitacao(any())).thenThrow(IdempotentTransactionDuplicatedException.class);

        Assertions.assertThrows(IdempotentTransactionDuplicatedException.class,
                () -> consumer.accept(message));

        verify(solicitacaoService, times(0)).processarCallbackWebhook(any());
        Mockito.verifyNoInteractions(acknowledgment);
    }

    @Test
    @DisplayName("Não deve processar evento quando toggle de consumo estiver desligada")
    void naoDeveProcessarEventoQuandoToggleDeConsumoEstiverDesligada(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        ReflectionTestUtils.setField(consumer, "eventToggle", "false");

        doNothing().when(metricService).incrementCounter(anyString());

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders));

        verifyNoInteractions(acknowledgment);
        verify(metricService, times(1)).incrementCounter(anyString());
        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Não deve processar evento quando for de erro por conflito de TransactionId")
    void naoDeveProcessarEventoQuandoForDeErroPorConflitoDeTransactionId(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        header.put("response-error-type", "CONFLICT");
        MessageHeaders messageHeaders = new MessageHeaders(header);

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders));

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoInteractions(solicitacaoService);
        verify(metricService, times(1)).incrementCounter(anyString());
    }
}