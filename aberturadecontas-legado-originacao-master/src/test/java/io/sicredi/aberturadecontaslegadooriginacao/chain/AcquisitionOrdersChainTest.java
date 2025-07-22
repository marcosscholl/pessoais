package io.sicredi.aberturadecontaslegadooriginacao.chain;

import br.com.sicredi.crm.ws.v1.carteiraservice.ConsultarCarteiraResponse;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.AcquisitionOrdersHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionOrdersClient;
import io.sicredi.aberturadecontaslegadooriginacao.client.CarteiraServiceSOAPClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionOrdersDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
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
import java.util.HashMap;
import java.util.List;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.ACQUISITION_ORDER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcquisitionOrdersChainTest {

    @Mock
    private AcquisitionOrdersClient acquisitionOrdersClient;
    @Mock
    private CarteiraServiceSOAPClient carteiraServiceSOAPClient;
    @InjectMocks
    private AcquisitionOrdersHandler acquisitionOrdersHandler;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;
    private static final String CODIGO_CARTEIRA = "209";

    @Test
    @DisplayName("Deve realizar processamento dos dados do pedido pois ainda não foi processado")
    void deveRealizarProcessamentoDoPedido() {
        var acquisitionOrdersDTO = Instancio.of(AcquisitionOrdersDTO.class).create();
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegadoSemCritica = Instancio.of(OriginacaoLegado.class).create();
        var originacaoLegado= Instancio.of(OriginacaoLegado.class).create();
        originacaoLegadoSemCritica.setCriticas(new ArrayList<>(0));
        originacaoLegadoSemCritica.setCadastros(new ArrayList<>(0));
        originacaoLegadoSemCritica.setProdutos(new ArrayList<>(0));
        originacaoLegadoSemCritica.setDetalheProduto(new HashMap<>(0));
        originacaoLegadoSemCritica.setCriticas(List.of(new Critica(ACQUISITION_ORDER, "Teste")));
        var consultarCarteiraResponse = new ConsultarCarteiraResponse();
        consultarCarteiraResponse.setCodigoCarteira(CODIGO_CARTEIRA);

        when(carteiraServiceSOAPClient.obterCodigoCarteira(anyString()))
                .thenReturn(consultarCarteiraResponse);

        when(acquisitionOrdersClient.buscaPedido(anyString()))
                .thenReturn(acquisitionOrdersDTO);

        when(originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(any(AcquisitionOrdersDTO.class))).thenReturn(originacaoLegado);

        acquisitionOrdersHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegadoSemCritica);

        verify(acquisitionOrdersClient, times(1)).buscaPedido(anyString());
    }

    @Test
    @DisplayName("Não deve realizar processamento dos dados do pedido pois já foi processado.")
    void naoDeveRealizarProcessamentoDoPedido() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(new ArrayList<>(0));

        acquisitionOrdersHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        verify(acquisitionOrdersClient, times(0)).buscaPedido(anyString());
    }

    @Test
    @DisplayName("Deve lançar BuscaPedidoNoAcquisitionOrdersException quando ocorrer erro no client")
    void deveLancarBuscaPedidoNoAcquisitionOrdersExceptionQuandoOcorrerErroNoClient() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setProdutos(new ArrayList<>(0));
        originacaoLegado.setCriticas(new ArrayList<>(0));

        when(acquisitionOrdersClient.buscaPedido(anyString()))
                .thenThrow(RetryableException.class);

        var novaOriginacaoLegado = acquisitionOrdersHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertEquals(1, novaOriginacaoLegado.getCriticas().size());
        verify(acquisitionOrdersClient, times(1)).buscaPedido(anyString());
    }

    @Test
    @DisplayName("Deve retornar null quando o serviço de consulta da carteira retornar uma string vazio")
    void deveRetornarNullQuandoServicoDeConsultaDeCarteiraRetornarStringVazio() {
        var acquisitionOrdersDTO = Instancio.of(AcquisitionOrdersDTO.class).create();
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegadoSemCritica = Instancio.of(OriginacaoLegado.class).create();
        var originacaoLegado= Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(new ArrayList<>(0));
        originacaoLegado.setCodigoCarteira(null);
        originacaoLegadoSemCritica.setCriticas(new ArrayList<>(0));
        originacaoLegadoSemCritica.setCadastros(new ArrayList<>(0));
        originacaoLegadoSemCritica.setProdutos(new ArrayList<>(0));
        originacaoLegadoSemCritica.setDetalheProduto(new HashMap<>(0));
        var consultarCarteiraResponse = new ConsultarCarteiraResponse();
        consultarCarteiraResponse.setCodigoCarteira(null);

        when(carteiraServiceSOAPClient.obterCodigoCarteira(anyString()))
                .thenReturn(consultarCarteiraResponse);

        when(acquisitionOrdersClient.buscaPedido(anyString()))
                .thenReturn(acquisitionOrdersDTO);

        when(originacaoLegadoMapper.mapAcquisitionOrdersDTOParaOriginacaoLegado(any(AcquisitionOrdersDTO.class))).thenReturn(originacaoLegado);

        var originacaoRetornado = acquisitionOrdersHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegadoSemCritica);

        assertFalse(originacaoRetornado.getCriticas().isEmpty());
        assertEquals(originacaoRetornado.getCodigoCarteira(), originacaoLegado.getCodigoCarteira() );
        verify(acquisitionOrdersClient, times(1)).buscaPedido(anyString());
    }
}