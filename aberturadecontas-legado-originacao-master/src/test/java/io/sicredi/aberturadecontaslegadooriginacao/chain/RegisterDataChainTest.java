package io.sicredi.aberturadecontaslegadooriginacao.chain;

import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.RegisterDataHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionRegisterDataClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.RegisterDataDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterDataChainTest {

    @Mock
    private AcquisitionRegisterDataClient acquisitionRegisterDataClient;
    @InjectMocks
    private RegisterDataHandler registerDataHandler;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;

    @Test
    @DisplayName("Deve realizar busca dos documentos do pedido com sucesso.")
    void deveRealizarBuscaDosDocumentosDoPedidoComSucesso() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());
        var acquisitionRegisterDataDTOEsperado = Instancio.of(RegisterDataDTO.class).create();
        var documentosEsperados = List.of(acquisitionRegisterDataDTOEsperado);

        when(acquisitionRegisterDataClient.buscarDocumentosDoPedido(anyString()))
                .thenReturn(documentosEsperados);

        doNothing().when(originacaoLegadoMapper).mapListaRegisterDataDTOParaOriginacaoLegado(anyList(), any(OriginacaoLegado.class));

        registerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        verify(acquisitionRegisterDataClient, times(1)).buscarDocumentosDoPedido(anyString());
    }

    @Test
    @DisplayName("Deve processar o evento, porém não deve fazer nada pois o evento já foi processado e os dados de register-data já foram adicionados a originação legado.")
    void naoDeveProcessarEventoDadosRegisterDataJaAdicionadosOriginacaoLegado() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(new ArrayList<>(0));

        registerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        verify(acquisitionRegisterDataClient, times(0)).buscarDocumentosDoPedido(anyString());
        verify(originacaoLegadoMapper, times(0)).mapListaRegisterDataDTOParaOriginacaoLegado(anyList(), any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve lançar BuscaDocumentosNoAcquisitionRegisterDataException quando ocorrer erro no client")
    void deveLancarBuscaDocumentosDoPedidoNoAcquisitionRegisterDataExceptionQuandoOcorrerErroNoClient() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());

        when(acquisitionRegisterDataClient.buscarDocumentosDoPedido(anyString()))
                .thenThrow(RetryableException.class);

        var novoOriginacaoLegado = registerDataHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertEquals(1, novoOriginacaoLegado.getCriticas().size());
        verify(acquisitionRegisterDataClient, times(1)).buscarDocumentosDoPedido(anyString());
    }
}