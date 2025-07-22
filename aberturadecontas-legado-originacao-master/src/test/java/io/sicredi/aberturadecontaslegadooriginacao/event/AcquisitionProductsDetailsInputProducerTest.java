package io.sicredi.aberturadecontaslegadooriginacao.event;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsDetailsInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcquisitionProductsDetailsInputProducerTest {

    private AcquisitionProductsDetailsInputProducer acquisitionProductsDetailsInputProducer;

    @Mock
    private StreamBridge streamBridge;

    @BeforeEach
    void setUp() {
        acquisitionProductsDetailsInputProducer = new AcquisitionProductsDetailsInputProducer(streamBridge);
    }

    @Test
    @DisplayName("Deve enviar mensagem para o tópico com sucesso")
    void deveEnviarMensagemParaTopico() {
        var mensagem = AcquisitionProductsDetailsInputDTO.builder().idPedido("123").idProdutoPedido("456").build();
        when(streamBridge.send(anyString(), any())).thenReturn(Boolean.TRUE);

        acquisitionProductsDetailsInputProducer.send(mensagem);

        verify(streamBridge, times(1)).send(anyString(), any());
    }

    @Test
    @DisplayName("Dado que ocorra um erro ao enviar mensagem para o tópico deve retornar error")
    void dadoQueOcorraErroAoEnviarMensagemDeveRetornarErro() {
        var mensagem = AcquisitionProductsDetailsInputDTO.builder().idPedido("854").idProdutoPedido("856").build();
        when(streamBridge.send(anyString(), any())).thenReturn(Boolean.FALSE);

        var resultado = assertThrows(InternalServerException.class, () -> acquisitionProductsDetailsInputProducer.send(mensagem));

        assertEquals("Erro ao enviar a mensagem para tópico acquisition-products-details-input-v1", resultado.getMessage());
    }
}