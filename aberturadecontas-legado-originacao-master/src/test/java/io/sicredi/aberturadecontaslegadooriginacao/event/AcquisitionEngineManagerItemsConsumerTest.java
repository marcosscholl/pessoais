package io.sicredi.aberturadecontaslegadooriginacao.event;

import io.sicredi.aberturadecontaslegadooriginacao.chain.OriginacaoFisitalLegadoChain;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import io.sicredi.aberturadecontaslegadooriginacao.service.MetricasService;
import io.sicredi.aberturadecontaslegadooriginacao.testdata.TestDataFactory;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AcquisitionEngineManagerItemsConsumerTest {

    @Mock
    private OriginacaoFisitalLegadoChain originacaoFisitalLegadoChain;
    @Mock
    private MetricasService metricasService;
    private AcquisitionEngineManagerItemsConsumer acquisitionEngineManagerItemsConsumer;
    private static Acknowledgment acknowledgmentMock;
    @Mock
    private OriginacaoLegadoRepository originacaoLegadoRepository;

    private static final String NOME_DA_METRICA = "event_acquisition_engine_manager_items";
    private static final String TAG_RESULTADO = "resultado";


    @BeforeAll
    static void configuraMocks() {
        reset();
        acknowledgmentMock = mock(Acknowledgment.class);
        doNothing().when(acknowledgmentMock).acknowledge();
    }

    @BeforeEach
    void limpaMocks() {
        acquisitionEngineManagerItemsConsumer = new AcquisitionEngineManagerItemsConsumer(metricasService, originacaoFisitalLegadoChain, originacaoLegadoRepository, true);
        clearInvocations(acknowledgmentMock);
    }

    @Test
    @DisplayName("Deve consumir e processar evento com produto do tipo CAPITAL_LEGACY e status STARTED, iniciar processamento da " +
            "originação, enviar métrica de sucesso e realizar acknowledge do evento.")
    void deveConsumirEProcessarEventoDeCapitalLegacyEStatusStarted() {
        var message = TestDataFactory.eventoSomenteCapitalLegacyStarted(acknowledgmentMock);

        doNothing().when(originacaoFisitalLegadoChain).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));

        acquisitionEngineManagerItemsConsumer.accept(message);

        verify(originacaoFisitalLegadoChain, times(1)).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));
        verify(metricasService, times(1)).incrementCounter(NOME_DA_METRICA, TAG_RESULTADO, "sucesso");
        verify(acknowledgmentMock, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Deve consumir e processar evento com único produto do tipo INVESTMENT_LEGACY e status STARTED," +
            " iniciar processamento da originação, enviar métrica de sucesso e realizar acknowledge do evento.")
    void deveConsumirEProcessarEventoComUnicoInvestmentLegacyEStatusStarted() {
        var message = TestDataFactory.eventoSomenteInvestmentLegacyStarted(acknowledgmentMock);

        doNothing().when(originacaoFisitalLegadoChain).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));

        acquisitionEngineManagerItemsConsumer.accept(message);

        verify(originacaoFisitalLegadoChain, times(1)).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));
        verify(metricasService, times(1)).incrementCounter(NOME_DA_METRICA, TAG_RESULTADO, "sucesso");
        verify(acknowledgmentMock, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Deve ignorar evento com produto do tipo CAPITAL_LEGACY e status diferente de STARTED, enviar métrica de " +
            "ignorado e realizar acknowledge do evento")
    void deveIgnorarEventoComProductTypeCapitalLegacyEStatusDiferenteDeStarted() {
        var message = TestDataFactory.eventoSomenteCapitalLegacyPending(acknowledgmentMock);

        deveIgnorarEvento(message);
    }

    @Test
    @DisplayName("Deve ignorar evento com produto do tipo INVESTMENT_LEGACY e status diferente de STARTED, enviar métrica de " +
            "ignorado e realizar acknowledge do evento")
    void deveIgnorarEventoComProductTypeInvestmentLegacyEStatusDiferenteDeStarted() {
        var message = TestDataFactory.eventoSomenteInvestmentLegacyPending(acknowledgmentMock);

        deveIgnorarEvento(message);
    }

    @Test
    @DisplayName("Deve ignorar evento sem produto do tipo CAPITAL_LEGACY ou INVESTMENT_LEGACY, enviar métrica de ignorado " +
            "e realizar acknowledge do evento")
    void deveIgnorarEventoSemProductTypeCapitalLegacyOuInvestmentLegacy() {
        var message = TestDataFactory.eventoSomenteIndividualCheckingAccountStarted(acknowledgmentMock);

        deveIgnorarEvento(message);
    }

    @Test
    @DisplayName("Deve ignorar evento com produto do tipo INVESTMENT_LEGACY e status STARTED e outros produtos relacionados," +
            " enviar métrica de ignorado e realizar acknowledge do evento")
    void deveIgnorarEventoComProductTypeInvestmentLegacyEStatusStartedComProdutosRelacionados() {
        var message = TestDataFactory.eventoInvestmentLegacyStartedComProdutosRelacionados(acknowledgmentMock);

        deveIgnorarEvento(message);
    }

    @Test
    @DisplayName("Deve lançar exceção e enviar métrica de erro quando ocorrer erro ao processar evento e não realizar acknowledge do evento")
    void deveLancarExcecaoEEnviarMetricaDeErroQuandoOcorrerErro() {
        var message = TestDataFactory.eventoSomenteCapitalLegacyStarted(acknowledgmentMock);

        doThrow(new RuntimeException("Erro genérico")).when(originacaoFisitalLegadoChain)
                .processaOriginacaoFisitalLegado(any(AcquisitionEngineManagerItemsEventDTO.class));

        assertThatThrownBy(() -> acquisitionEngineManagerItemsConsumer.accept(message))
                .isInstanceOf(RuntimeException.class)
                .message().contains("Erro genérico");

        verify(originacaoFisitalLegadoChain, times(1)).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));

        verify(metricasService, times(1)).incrementCounter(NOME_DA_METRICA, TAG_RESULTADO, "erro");
        verify(acknowledgmentMock, times(0)).acknowledge();
    }

    @Test
    @DisplayName("Não deve processar o evento")
    public void NaoDeveProcessarMensagemNoConsumer() {
        var message = TestDataFactory.eventoSomenteCapitalLegacyStarted(acknowledgmentMock);
        AcquisitionEngineManagerItemsConsumer consumer = new AcquisitionEngineManagerItemsConsumer(metricasService, originacaoFisitalLegadoChain, originacaoLegadoRepository, false);

        consumer.accept(message);

        verify(originacaoFisitalLegadoChain, times(0)).processaOriginacaoFisitalLegado(any());
        verify(acknowledgmentMock, times(0)).acknowledge();
    }

    private void deveIgnorarEvento(Message<String> message) {
        acquisitionEngineManagerItemsConsumer.accept(message);

        verifyNoInteractions(originacaoFisitalLegadoChain);
        verify(metricasService, times(1)).incrementCounter(NOME_DA_METRICA, TAG_RESULTADO, "ignorado");
        verify(acknowledgmentMock, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Deve ignorar processamento pois existe um pedido interno em procesamento ou finalizado")
    void deveIgnorarPoisProcessamentoInternoFinalizado() {
        var message = TestDataFactory.eventoSomenteInvestmentLegacyStarted(acknowledgmentMock);
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        when(originacaoLegadoRepository.findByIdPedido(any())).thenReturn(Optional.of(originacaoLegado));

        acquisitionEngineManagerItemsConsumer.accept(message);

        verify(originacaoFisitalLegadoChain, times(0)).processaOriginacaoFisitalLegado(any(
                AcquisitionEngineManagerItemsEventDTO.class));
    }
}