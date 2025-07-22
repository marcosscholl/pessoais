package io.sicredi.aberturadecontaslegadooriginacao.event;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsCreationInputDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcquisitionProductsCreationInputProducerTest {

    private AcquisitionProductsCreationInputProducer acquisitionProductsCreationInputProducer;

    @Mock
    private StreamBridge streamBridge;

    @BeforeEach
    void setUp() {
        this.acquisitionProductsCreationInputProducer = new AcquisitionProductsCreationInputProducer(streamBridge);
    }

    @Test
    @DisplayName("Deve enviar mensagem para o tópico com sucesso")
    void deveEnviarMensagemParaTopico() {
        var mensagem = AcquisitionProductsCreationInputDTO.builder().idPedido("522").idProdutoPedido("963").build();
        when(streamBridge.send(anyString(), any())).thenReturn(Boolean.TRUE);

        acquisitionProductsCreationInputProducer.send(mensagem);

        verify(streamBridge, times(1)).send(anyString(), any());
    }

    @Test
    @DisplayName("Dado que ocorra um erro ao enviar mensagem para o tópico deve retornar error")
    void dadoQueOcorraErroAoEnviarMensagemDeveRetornarErro() {
        var mensagem = AcquisitionProductsCreationInputDTO.builder().idPedido("754").idProdutoPedido("755").build();
        when(streamBridge.send(anyString(), any())).thenReturn(Boolean.FALSE);

        var resultado = assertThrows(InternalServerException.class, () -> acquisitionProductsCreationInputProducer.send(mensagem));

        assertEquals("Erro ao enviar a mensagem para tópico acquisition-products-creation-input-v1", resultado.getMessage());
    }
}