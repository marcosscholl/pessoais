package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.BureauRFService;
import io.sicredi.aberturadecontasalarioefetivador.service.MetricService;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentTransactionDuplicatedException;
import io.sicredi.engineering.libraries.idempotent.transaction.JsonUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessaBureauRFContaSalarioConsumerTest {

    @Mock
    Acknowledgment acknowledgment;
    @Mock
    BureauRFService bureauRFService;
    @Mock
    MetricService metricService;

    @InjectMocks
    ProcessaBureauRFContaSalarioConsumer consumer;

    @BeforeEach
    void defaultMocks(){
        doNothing().when(metricService).incrementCounter(anyString());
    }

    @Test
    @DisplayName("Deve processar evento com sucesso")
    void deveProcessarEventoComSucesso(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastros().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders));

        verify(bureauRFService, times(1)).processarSolicitacaoBureauRF(any());
        verify(acknowledgment, times(1)).acknowledge();
        verify(metricService, times(1)).incrementCounter(anyString());
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao processar evento de erro")
    void naoDeveProcessarEventoComConflitoDeTransactionId(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastros().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        Message<String> message = MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro), messageHeaders);

        when(bureauRFService.processarSolicitacaoBureauRF(any())).thenThrow(IdempotentTransactionDuplicatedException.class);

        Assertions.assertThrows(IdempotentTransactionDuplicatedException.class,
                () -> consumer.accept(message));

        verifyNoInteractions(acknowledgment);
        verify(metricService, times(1)).incrementCounter(anyString());
    }

    @Test
    @DisplayName("Não deve processar nem dar ack no evento quando toggle de pause estiver ligada")
    void naoDeveProcessarEventoQuandoToggleDePauseEstiverLigada(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastros().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        ReflectionTestUtils.setField(consumer, "pauseToggle", "true");

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders));

        verifyNoInteractions(acknowledgment);
        verifyNoInteractions(bureauRFService);
        verify(metricService, times(1)).incrementCounter(anyString());
    }

    @Test
    @DisplayName("Não deve processar evento quando toggle de consumo estiver desligada")
    void naoDeveProcessarEventoQuandoToggleDeConsumoEstiverDesligada(){
        Cadastro cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastros().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        ReflectionTestUtils.setField(consumer, "enabledToggle", "false");

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders));

        verify(acknowledgment, times(1)).acknowledge();
        verifyNoInteractions(bureauRFService);
        verify(metricService, times(1)).incrementCounter(anyString());
    }
}