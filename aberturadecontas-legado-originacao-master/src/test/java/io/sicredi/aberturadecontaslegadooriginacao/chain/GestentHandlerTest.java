package io.sicredi.aberturadecontaslegadooriginacao.chain;

import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.GestentHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.GestentConectorClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CodigoEntidadeDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestentHandlerTest {

    @Mock
    private GestentConectorClient gestentConectorClient;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;
    @InjectMocks
    private GestentHandler gestentHandler;

    @Test
    @DisplayName("Deve realizar busca do código da entidade")
    void deveRealizarBuscaDoCodigoEntidade() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());
        CodigoEntidadeDTO codigoEntidadeMock = Instancio.of(CodigoEntidadeDTO.class).create();

        when(gestentConectorClient.buscarCodigoEntidade(anyString(), anyString()))
                .thenReturn(codigoEntidadeMock);

        doNothing().when(originacaoLegadoMapper).merge(any(OriginacaoLegado.class), any(OriginacaoLegado.class));

        gestentHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        verify(gestentConectorClient, times(1)).buscarCodigoEntidade(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve realizar busca do código da entidade e não encontrar nenhum código para a cooperativa e agência informado.")
    void deveRealizarBuscaDoCodigoEntidadeENaoEncontrar() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());
        when(gestentConectorClient.buscarCodigoEntidade(anyString(), anyString()))
                .thenReturn(null);

        var novaOriginacaoLegado = gestentHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertEquals(1, novaOriginacaoLegado.getCriticas().size());

        verify(gestentConectorClient, times(1)).buscarCodigoEntidade(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar BuscaCodigoEntidadeNoGestentConectorException quando ocorrer erro no client")
    void deveLancarBuscaCodigoEntidadeNoGestentConectorExceptionQuandoOcorrerErroNoClient() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());

        when(gestentConectorClient.buscarCodigoEntidade(anyString(), anyString()))
                .thenThrow(RetryableException.class);

        var novaOriginacaoLegado = gestentHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertEquals(1, novaOriginacaoLegado.getCriticas().size());
        verify(gestentConectorClient, times(1)).buscarCodigoEntidade(anyString(), anyString());
    }

}