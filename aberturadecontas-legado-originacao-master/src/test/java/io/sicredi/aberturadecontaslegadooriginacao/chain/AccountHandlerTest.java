package io.sicredi.aberturadecontaslegadooriginacao.chain;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtilResponse;
import br.com.sicredi.mua.commons.business.server.ejb.OutProximoDiaUtil;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.AccountHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionCheckingAccountClient;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionOrdersClient;
import io.sicredi.aberturadecontaslegadooriginacao.client.MonthlyFeeSimulationClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionOrdersDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DadosSimulacaoCestaRelacionamentoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.NumeroContaDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.Critica;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.capital.acquisition.grpc.AcquisitionConfigurationServiceGrpc;
import io.sicredi.capital.acquisition.grpc.ConfigurationDTO;
import io.sicredi.capital.acquisition.grpc.IdentifierDTO;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountHandlerTest {
    @Mock
    private MonthlyFeeSimulationClient monthlyFeeSimulationClient;
    @Mock
    private AcquisitionCheckingAccountClient acquisitionCheckingAccountClient;
    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;
    @InjectMocks
    private AccountHandler accountHandler;
    private static final String PRODUCT_ACCOUNT_LEGACY = "ACCOUNT_LEGACY";

    @Test
    @DisplayName("Deve realizar processamento do produto ACCOUNT_LEGACY com sucesso")
    void deveRealizarProcessamentoDoProdutoAccountLegacyComSucesso() {
        var dadosSimulacaoCestaRelacionamentoDTO = Instancio.of(DadosSimulacaoCestaRelacionamentoDTO.class).create();
        var dadosNumeroConta = Instancio.of(NumeroContaDTO.class).create();
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var originacaoLegadoRetornoMock = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProdutoMock = new HashMap<>(0);
        detalhesProdutoMock.put(PRODUCT_ACCOUNT_LEGACY, Instancio.of(DetalheProduto.class).create());
        originacaoLegadoRetornoMock.setDetalheProduto(detalhesProdutoMock);
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setConfiguracao(null);
        detalheProduto.setNumeroConta(null);
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        detalhesProduto.put(PRODUCT_ACCOUNT_LEGACY, detalheProduto);
        originacaoLegado.setDetalheProduto(detalhesProduto);

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoRetornoMock);

        when(monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(anyString()))
                .thenReturn(dadosSimulacaoCestaRelacionamentoDTO);

        when(acquisitionCheckingAccountClient.buscarNumeroConta(anyString(), anyString()))
                .thenReturn(dadosNumeroConta);

        var resultadoProcessamento = accountHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertNotNull(resultadoProcessamento.getDetalheProduto().get(PRODUCT_ACCOUNT_LEGACY));
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(PRODUCT_ACCOUNT_LEGACY).getConfiguracao());
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(PRODUCT_ACCOUNT_LEGACY).getConfiguracao().getCestaRelacionamento());
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(PRODUCT_ACCOUNT_LEGACY).getConfiguracao().getCestaRelacionamento().getId());
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(PRODUCT_ACCOUNT_LEGACY).getConfiguracao().getCestaRelacionamento().getDiaPagamento());
        verify(monthlyFeeSimulationClient, times(1)).buscarDadosCestaRelacionamento(anyString());
        verify(acquisitionCheckingAccountClient, times(1)).buscarNumeroConta(anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve realizar processamento do produto ACCOUNT_LEGACY pois o mesmo já foi processado")
    void naoDeveRealizarProcessamentoDoProdutoAccountLegacyJaProcessado() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        detalhesProduto.put(PRODUCT_ACCOUNT_LEGACY, Instancio.of(DetalheProduto.class).create());
        originacaoLegado.setCriticas(new ArrayList<>(0));
        originacaoLegado.setDetalheProduto(detalhesProduto);

        accountHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        verify(monthlyFeeSimulationClient, times(0)).buscarDadosCestaRelacionamento(anyString());
        verify(acquisitionCheckingAccountClient, times(0)).buscarNumeroConta(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar um erro quando o servico de consulta do numero de conta for chamado")
    void deveLancarErroQuandoServicoNumeroContaForChamado() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        var detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setConfiguracao(null);
        detalhesProduto.put(PRODUCT_ACCOUNT_LEGACY, detalheProduto);
        originacaoLegado.setDetalheProduto(detalhesProduto);
        originacaoLegado.setCriticas(new ArrayList<>(0));
        var originacaoLegadoRetornoMock = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProdutoMock = new HashMap<>(0);
        var detalheProdutoMock =  Instancio.of(DetalheProduto.class).create();
        detalheProdutoMock.setConfiguracao(null);
        detalhesProdutoMock.put(PRODUCT_ACCOUNT_LEGACY, detalheProdutoMock);
        originacaoLegadoRetornoMock.setDetalheProduto(detalhesProdutoMock);
        originacaoLegadoRetornoMock.setCriticas(new ArrayList<>(0));

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoRetornoMock);

        when(acquisitionCheckingAccountClient.buscarNumeroConta(anyString(), anyString())).thenThrow(NotFoundException.class);

        var retornoProcessamento = accountHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertTrue(!retornoProcessamento.getCriticas().isEmpty());
        assertEquals(EtapaProcessoOriginacao.PRODUCT_ACCOUNT_LEGACY, retornoProcessamento.getCriticas().getFirst().getCodigo());
    }

    @Test
    @DisplayName("Não deve processar produto ACCOUNT_LEGACY pois não foi encontrada nenhuma configuração para o id de simulação informado")
    void naoDeveProcessarProdutoConfiguracaoNaoEncontradaParaIdSimulacao() {
        var dadosSimulacaoCestaRelacionamentoDTO = Instancio.of(DadosSimulacaoCestaRelacionamentoDTO.class).create();
        var dadosNumeroConta = Instancio.of(NumeroContaDTO.class).create();
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegadoRetornoMock = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProdutoMock = new HashMap<>(0);
        var detalheProdutoMock =  Instancio.of(DetalheProduto.class).create();
        detalheProdutoMock.setConfiguracao(null);
        detalhesProdutoMock.put(PRODUCT_ACCOUNT_LEGACY, detalheProdutoMock);
        originacaoLegadoRetornoMock.setDetalheProduto(detalhesProdutoMock);
        originacaoLegadoRetornoMock.setCriticas(new ArrayList<>(0));
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        var detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setConfiguracao(null);
        detalhesProduto.put(PRODUCT_ACCOUNT_LEGACY, detalheProduto);
        originacaoLegado.setDetalheProduto(detalhesProduto);
        originacaoLegado.setCriticas(new ArrayList<>(0));

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoRetornoMock);

        when(monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(anyString()))
                .thenThrow(new NotFoundException());

        var retornoProcessamento = accountHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertTrue(!retornoProcessamento.getCriticas().isEmpty());
        assertEquals(EtapaProcessoOriginacao.PRODUCT_ACCOUNT_LEGACY, retornoProcessamento.getCriticas().getFirst().getCodigo());
        assertEquals("Erro ao processar os dados do produto [ ACCOUNT_LEGACY ] para o pedido [ "+retornoProcessamento.getIdPedido()+" ]", retornoProcessamento.getCriticas().getFirst().getDescricao());
    }
}

