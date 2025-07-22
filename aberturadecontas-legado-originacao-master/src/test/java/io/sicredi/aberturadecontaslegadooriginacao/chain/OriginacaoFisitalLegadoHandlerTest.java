package io.sicredi.aberturadecontaslegadooriginacao.chain;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.OriginacaoFisitalLegadoHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.OriginacaoLegadoClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.PedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoDTOMapper;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OriginacaoFisitalLegadoHandlerTest {
    @Mock
    private OriginacaoLegadoClient originacaoLegadoClient;
    @Mock
    private OriginacaoLegadoRepository originacaoLegadoRepository;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;
    @Mock
    private OriginacaoLegadoDTOMapper originacaoLegadoDTOMapper;
    @InjectMocks
    private OriginacaoFisitalLegadoHandler originacaoFisitalLegadoHandler;

    @Test
    @DisplayName("Deve realizar o envio da originação legado para o BPL")
    void deveRealizarEnvioDaOriginacaoParaBPL() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(new ArrayList<>(0));
        originacaoLegado.getCadastros().getFirst().setCoreId(null);

        doNothing().when(originacaoLegadoClient).processaOriginacao(any(PedidoDTO.class));

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);

        var novaOriginacaoLegado = originacaoFisitalLegadoHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertTrue(novaOriginacaoLegado.getCriticas().isEmpty());
        verify(originacaoLegadoClient, times(1)).processaOriginacao(any(PedidoDTO.class));
    }

    @Test
    @DisplayName("Deve tentar enviar para o BPL uma originação não completo.")
    void deveTentarProcessarOriginacaoNaoCompleta() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCodigoCarteira(null);

        var originacaoLegadoMock = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegadoMock.setCriticas(new ArrayList<>(0));

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoMock);

        var novaOriginacaoLegado = originacaoFisitalLegadoHandler.processarProximo(acquisitionEngineManagerItemsEventDTO,originacaoLegado);

        assertFalse(novaOriginacaoLegado.getCriticas().isEmpty());
        verify(originacaoLegadoClient, times(0)).processaOriginacao(any(PedidoDTO.class));
    }

    @Test
    @DisplayName("Deve gerar critica de originação não completo quando ocorrer erro na chamada de envio dos dados ao BPL")
    void deveLancarCriticaQuandoOcorrerErroChamadaEnvioDadosBPLComDadosIncompletos() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setIdPedido(acquisitionEngineManagerItemsEventDTO.idPedido());

        var originacaoLegadoMock = Instancio.of(OriginacaoLegado.class).create();

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoMock);

        var novaOriginacaoLegado = originacaoFisitalLegadoHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertFalse(novaOriginacaoLegado.getCriticas().isEmpty());
        verify(originacaoLegadoRepository, times(1)).save(any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando ocorrer erro na chamada de envio dos dados ao BPL")
    void deveLancarExceptionQuandoOcorrerErroChamadaEnvioDadosBPL() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setIdPedido(acquisitionEngineManagerItemsEventDTO.idPedido());
        originacaoLegado.setCriticas(new ArrayList<>(0));
        var originacaoLegadoMock = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoMock);

        doThrow(new RuntimeException("Test Exception"))
                .when(originacaoLegadoClient)
                .processaOriginacao(any(PedidoDTO.class));

        var novaOriginacaoLegado = originacaoFisitalLegadoHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertFalse(novaOriginacaoLegado.getCriticas().isEmpty());
        verify(originacaoLegadoClient, times(1)).processaOriginacao(any(PedidoDTO.class));
    }

    @Test
    @DisplayName("Deve realizar consulta de uma originação legado por id do pedido na base de dados")
    void deveRealizarConsultaOriginacaoLegadoPorIdPedidoComSucesso() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var originacaoLegadoDTO = Instancio.of(OriginacaoLegadoDTO.class).create();
        String idPedido = "xpto";

        when(originacaoLegadoRepository.findByIdPedido(anyString()))
                .thenReturn(Optional.of(originacaoLegado));

        when(originacaoLegadoDTOMapper.map(any(OriginacaoLegado.class)))
                .thenReturn(originacaoLegadoDTO);

        var novaOriginacaoLegadoDTO = originacaoFisitalLegadoHandler.buscarOriginacaoLegadoPorIdPedido(idPedido);

        assertNotNull(novaOriginacaoLegadoDTO);
        verify(originacaoLegadoRepository, times(1)).findByIdPedido(anyString());
        verify(originacaoLegadoDTOMapper, times(1)).map(any(OriginacaoLegado.class));
    }

    @Test
    @DisplayName("Deve lançar erro NotFoundException na consulta originação legado por id do pedido na base de dados")
    void deveLancarErroNotFoundExceptionConsultaOriginacaoLegadoPorIdPedido() {
        String idPedido = "xpto";

        when(originacaoLegadoRepository.findByIdPedido(anyString()))
                .thenThrow(NotFoundException.class);

        var novaOriginacaoLegadoDTO = originacaoFisitalLegadoHandler.buscarOriginacaoLegadoPorIdPedido(idPedido);

        assertNull(novaOriginacaoLegadoDTO);
        verify(originacaoLegadoRepository, times(1)).findByIdPedido(anyString());
        verify(originacaoLegadoDTOMapper, times(0)).map(any(OriginacaoLegado.class));
    }

}