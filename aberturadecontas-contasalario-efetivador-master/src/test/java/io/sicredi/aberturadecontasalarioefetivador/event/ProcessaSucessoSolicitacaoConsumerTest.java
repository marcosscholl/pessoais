package io.sicredi.aberturadecontasalarioefetivador.event;

import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.service.ContaSalarioService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessaSucessoSolicitacaoConsumerTest {

    @Mock
    Acknowledgment acknowledgment;
    @Mock
    ContaSalarioService contaSalarioService;
    @Mock
    MetricService metricService;
    @InjectMocks
    ProcessaSucessoCadastroContaSalarioConsumer consumer;

    @BeforeEach
    void defaultMocks(){
        doNothing().when(metricService).incrementCounter(anyString());
    }

    @Test
    @DisplayName("Deve processar evento com sucesso")
    void deveProcessarEventoComSucesso(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verify(contaSalarioService, times(1)).processarRespostaSolicitacaoUnitaria(any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao processar evento de erro")
    void naoDeveProcessarEventoComConflitoDeTransactionId(){
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        HashMap<String, Object> header = new HashMap<>();
        header.put(KafkaHeaders.ACKNOWLEDGMENT, acknowledgment);
        MessageHeaders messageHeaders = new MessageHeaders(header);
        Message<String> message = MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro), messageHeaders);

        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any())).thenThrow(IdempotentTransactionDuplicatedException.class);

        Assertions.assertThrows(IdempotentTransactionDuplicatedException.class,
                () -> consumer.accept(message));

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

        consumer.accept(MessageBuilder.createMessage(JsonUtil.writeToJson(cadastro),messageHeaders ));

        verify(acknowledgment, times(1)).acknowledge();
        verify(metricService, times(1)).incrementCounter(anyString());
        verifyNoInteractions(contaSalarioService);
    }
}