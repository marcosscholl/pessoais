package io.sicredi.aberturadecontaslegadooriginacao.chain;

import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.InvestimentHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.service.NumeroContaService;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.MappingException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestimentHandlerTest {

    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;
    @Mock
    private NumeroContaService numeroContaService;
    @InjectMocks
    private InvestimentHandler handler;

    private static final String POUPANCA = "INVESTMENT_LEGACY";
    private static final String CONTA_CORRENTE = "ACCOUNT_LEGACY";
    private static final String CAPITAL = "CAPITAL_LEGACY";

    @Test
    @DisplayName("Deve preencher numero da conta para o produto INVESTIMENT_LEGACY com o mesmo numero da conta corrente quando o pedido possuir um item ACCOUNT_LEGACY")
    void devePreencherNumeroContaPoupancaComNumeroDaContaCorrente() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());

        var originacaoMock = prepararMock(originacaoLegado, CONTA_CORRENTE);

        var resultadoProcessamento = handler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertNotNull(resultadoProcessamento.getDetalheProduto().get(POUPANCA));
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(POUPANCA).getNumeroConta());
        assertEquals(resultadoProcessamento.getDetalheProduto().get(POUPANCA).getNumeroConta(), originacaoMock.getDetalheProduto().get(POUPANCA).getNumeroConta());
    }

    @Test
    @DisplayName("Deve preencher numero da conta para o produto INVESTIMENT_LEGACY quando não existir nenhum item ACCOUNT_LEGACY")
    void DevePreencherNumeroContaPoupancaQuandoNaoTiverNenhumItemDeContaCorrenteNoPedido() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());
        prepararMock(originacaoLegado, CAPITAL);
        var resultadoProcessamento = handler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertNotNull(resultadoProcessamento.getDetalheProduto().get(POUPANCA));
        assertNotNull(resultadoProcessamento.getDetalheProduto().get(POUPANCA).getNumeroConta());
    }

    @Test
    @DisplayName("Deve lançar um erro quando tentar mapear os dados da originação.")
    void deveAdicionarCriticaQuandoOcorrerErroNoMapeamentoDosDadosDaOriginacao() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = new OriginacaoLegado(acquisitionEngineManagerItemsEventDTO.idPedido());
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setConfiguracao(null);
        detalheProduto.setNumeroConta(null);
        detalhesProduto.put(POUPANCA, detalheProduto);
        originacaoLegado.setDetalheProduto(detalhesProduto);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class)))
                .thenThrow(MappingException.class);

        var novaOriginacaoLegado = handler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(1, novaOriginacaoLegado.getCriticas().size());
    }

    private OriginacaoLegado prepararMock(OriginacaoLegado originacaoLegado, String nomeProduto) {
        var detalheProdutoAdicional = Instancio.of(DetalheProduto.class).create();

        var originacaoLegadoRetornoMock = Instancio.of(OriginacaoLegado.class).create();
        Map<String, DetalheProduto> detalhesProdutoMock = new HashMap<>(0);
        var detalheInvestimentLegacyMock = Instancio.of(DetalheProduto.class).create();
        detalheInvestimentLegacyMock.setNumeroConta(null);
        detalhesProdutoMock.put(nomeProduto, detalheProdutoAdicional);
        detalhesProdutoMock.put(POUPANCA, detalheInvestimentLegacyMock);
        originacaoLegadoRetornoMock.setDetalheProduto(detalhesProdutoMock);

        DetalheProduto detalheProdutoInvestimentLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoInvestimentLegacy.setConfiguracao(null);
        detalheProdutoInvestimentLegacy.setNumeroConta(null);
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>(0);
        detalhesProduto.put(POUPANCA, detalheProdutoInvestimentLegacy);
        originacaoLegado.setDetalheProduto(detalhesProduto);

        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegadoRetornoMock);
        when(numeroContaService.obterNumeroConta(any(OriginacaoLegado.class))).thenReturn("123456");

        return originacaoLegadoRetornoMock;
    }

}