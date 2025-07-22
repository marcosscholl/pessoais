package io.sicredi.aberturadecontaslegadooriginacao.entities;

import org.apache.commons.lang3.StringUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OriginacaoLegadoTest {

    private static final String ID_PEDIDO = "XPTO";
    private static final String MENSAGEM_ERRO = "Erro";
    private static final String NUMERO_CONTA = "123456789";
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";
    private static final String PRODUTO_ACCOUNT_LEGACY = "ACCOUNT_LEGACY";

    @Test
    @DisplayName("Deve retornar verdadeiro quando todos os campos obrigatórios estão preenchidos")
    void deveRetornarVerdadeiroQuandoTodosCamposObrigatoriosEstaoPreenchidos() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(new ArrayList<>(0));
        assertTrue(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo id não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoIdNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setId(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo idPedido não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoIdPedidoPedidoNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setIdPedido(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo cooperativa não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoCooperativaNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCooperativa(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo agencia não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoAgenciaNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setAgencia(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo codigo carteira não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoCodigoCarteiraNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCodigoCarteira(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo codigo entidade não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoCodigoEntidadeNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCodigoEntidade(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo status não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoStatusNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setStatus(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo canal não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoCanalNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCanal(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo origem não estiver preenchido")
    void deveRetornarFalseQuandoTodoCampoOrigemNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setOrigem(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando a lista de cadastros por não estiver preenchido")
    void deveRetornarFalseQuandoListaDeCadastrosNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCadastros(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando a lista de documentos por não estiver preenchido")
    void deveRetornarFalseQuandoListaDeDocumentosNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setDocumentos(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando a lista de produtos por não estiver preenchido")
    void deveRetornarFalseQuandoListaDeProdutosNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setProdutos(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando a lista de detalhes de produtos por não estiver preenchido")
    void deveRetornarFalseQuandoListaDeDetalheProdutosNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setDetalheProduto(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve retornar false quando o campo criado por por não estiver preenchido")
    void deveRetornarFalseQuandoCampoCriadoPorNaoTiverValorValido() {
        OriginacaoLegado originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriadoPor(null);
        assertFalse(originacaoLegado.isCompleto());
    }

    @Test
    @DisplayName("Deve adicionar uma crítica à lista de críticas")
    void deveAdicionarUmaCritica() {
        OriginacaoLegado originacaoLegado = new OriginacaoLegado(ID_PEDIDO);
        Critica critica = new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, MENSAGEM_ERRO);

        originacaoLegado.adicionarCritica(critica);

        assertNotNull(originacaoLegado.getCriticas());
        assertEquals(1, originacaoLegado.getCriticas().size());
        assertEquals(critica, originacaoLegado.getCriticas().getFirst());
    }

    @Test
    @DisplayName("Deve remover uma crítica à lista de críticas")
    void deveRemoverUmaCritica() {
        OriginacaoLegado originacaoLegado = new OriginacaoLegado(ID_PEDIDO);
        List<Critica> listaCriticas = new ArrayList<>(0);
        listaCriticas.add(new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, MENSAGEM_ERRO));
        originacaoLegado.setCriticas(listaCriticas);

        originacaoLegado.removerCritica(EtapaProcessoOriginacao.CUSTOMER_DATA);

        assertNotNull(originacaoLegado.getCriticas());
        assertEquals(0, originacaoLegado.getCriticas().size());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro se a crítica existe para a etapa especificada")
    void deveRetornarVerdadeiroSeCriticaExisteParaEtapaEspecificada() {
        OriginacaoLegado originacaoLegado = new OriginacaoLegado(ID_PEDIDO);
        Critica critica = new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, MENSAGEM_ERRO);
        originacaoLegado.adicionarCritica(critica);

        assertTrue(originacaoLegado.temCritica(EtapaProcessoOriginacao.CUSTOMER_DATA));
    }

    @Test
    @DisplayName("Deve retornar falso se a crítica não existe para a etapa especificada")
    void deveRetornarFalsoSeCriticaNaoExisteParaEtapaEspecificada() {
        OriginacaoLegado originacaoLegado = new OriginacaoLegado(ID_PEDIDO);

        assertFalse(originacaoLegado.temCritica(EtapaProcessoOriginacao.CUSTOMER_DATA));
    }

    @Test
    @DisplayName("Deve limpar todas as críticas")
    void deveLimparTodasAsCriticas() {
        OriginacaoLegado originacaoLegado = new OriginacaoLegado(ID_PEDIDO);
        Critica critica = new Critica(EtapaProcessoOriginacao.CUSTOMER_DATA, MENSAGEM_ERRO);
        originacaoLegado.adicionarCritica(critica);

        originacaoLegado.limparCriticas();

        assertNotNull(originacaoLegado.getCriticas());
        assertTrue(originacaoLegado.getCriticas().isEmpty());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando todos os campos necessários para acquisitionOrderProcessado estão preenchidos")
    void deveRetornarVerdadeiroQuandoAcquisitionOrderProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .idPedido("123")
                .cooperativa("cooperativa")
                .agencia("agencia")
                .status("status")
                .canal("canal")
                .origem("origem")
                .criadoPor("criador")
                .cadastros(List.of(new Cadastro()))
                .produtos(List.of(CAPITAL_LEGACY))
                .detalheProduto(Map.of(CAPITAL_LEGACY, new DetalheProduto()))
                .build();

        assertTrue(originacaoLegado.acquisitionOrderProcessado());
    }

    @Test
    @DisplayName("Deve retornar falso quando algum campo necessário para acquisitionOrderProcessado está preenchido com string vazio")
    void deveRetornarFalsoQuandoAcquisitionOrderNaoProcessadoComStringVazio() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .idPedido(ID_PEDIDO)
                .cooperativa("")
                .agencia(" ")
                .status(" ")
                .canal(null)
                .origem(" ")
                .criadoPor("")
                .build();

        var resultado = originacaoLegado.acquisitionOrderProcessado();

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Deve retornar falso quando algum campo necessário para acquisitionOrderProcessado está preenchido com null")
    void deveRetornarFalsoQuandoAcquisitionOrderComValoresNull() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .idPedido(ID_PEDIDO)
                .cooperativa(null)
                .agencia(null)
                .status(null)
                .canal(null)
                .origem(null)
                .criadoPor(null)
                .build();

        var resultado = originacaoLegado.acquisitionOrderProcessado();

        assertFalse(resultado);
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando a lista de cadastros não está vazia")
    void deveRetornarVerdadeiroQuandoCustomerDataProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .cadastros(List.of(Instancio.of(Cadastro.class).create()))
                .build();

        assertTrue(originacaoLegado.customerDataProcessado());
    }

    @Test
    @DisplayName("Deve retornar falso quando a lista de cadastros está vazia")
    void deveRetornarFalsoQuandoCustomerDataNaoProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .cadastros(new ArrayList<>())
                .build();

        assertFalse(originacaoLegado.customerDataProcessado());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando a lista de documentos não está vazia")
    void deveRetornarVerdadeiroQuandoRegisterDataProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .documentos(List.of(new Documento()))
                .build();

        assertTrue(originacaoLegado.registerDataProcessado());
    }

    @Test
    @DisplayName("Deve retornar falso quando a lista de documentos está vazia")
    void deveRetornarFalsoQuandoRegisterDataNaoProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .documentos(new ArrayList<>())
                .build();

        assertFalse(originacaoLegado.registerDataProcessado());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando codigoEntidade está preenchido")
    void deveRetornarVerdadeiroQuandoGestentProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .codigoEntidade("codigo")
                .build();

        assertTrue(originacaoLegado.gestentProcessado());
    }

    @Test
    @DisplayName("Deve retornar falso quando codigoEntidade não está preenchido")
    void deveRetornarFalsoQuandoGestentNaoProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .build();

        assertFalse(originacaoLegado.gestentProcessado());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando detalheProduto contém configuração")
    void deveRetornarVerdadeiroQuandoConfiguracaoProcessado() {
        DetalheProduto detalheProduto = new DetalheProduto();
        detalheProduto.setConfiguracao(new ConfiguracaoDetalhe());
        detalheProduto.getConfiguracao().setCapital(new Capital());
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(CAPITAL_LEGACY, detalheProduto))
                .build();

        assertTrue(originacaoLegado.configuracaoCapitalLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar falso quando detalheProduto não contém configuração")
    void deveRetornarFalsoQuandoConfiguracaoNaoProcessado() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(CAPITAL_LEGACY, new DetalheProduto()))
                .build();

        assertFalse(originacaoLegado.configuracaoCapitalLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar true quando o produto ACCOUNT_LEGACY estiver processado com sucesso")
    void deveRetornarTrueQuandoProdutoAccountLegacyForProcessadoComSucesso() {
        ConfiguracaoDetalhe configuracaoDetalheAccountLegacy = Instancio.of(ConfiguracaoDetalhe.class).create();
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setNumeroConta(NUMERO_CONTA);
        detalheProduto.setConfiguracao(configuracaoDetalheAccountLegacy);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(PRODUTO_ACCOUNT_LEGACY, detalheProduto))
                .build();

        assertTrue(originacaoLegado.configuracaoAccountLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar false quando não tiver numero de conta preenchido")
    void deveRetornarFalseQuandoNaoTiverNumeroContaPreenchido() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setNumeroConta("");
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(PRODUTO_ACCOUNT_LEGACY, detalheProduto))
                .build();

        assertFalse(originacaoLegado.configuracaoAccountLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar false quando não tiver dados da cesta de relacionamento")
    void deveRetornarFalseQuandoNaoTiverDadosCestaRelacionamentoForNull() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setNumeroConta(NUMERO_CONTA);
        detalheProduto.getConfiguracao().setCestaRelacionamento(null);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(PRODUTO_ACCOUNT_LEGACY, detalheProduto))
                .build();

        assertFalse(originacaoLegado.configuracaoAccountLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar false quando não tiver dados da cesta de relacionamento")
    void deveRetornarFalseQuandoNaoTiverConfiguracaoParaProdutoAccountLegacy() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setNumeroConta(NUMERO_CONTA);
        detalheProduto.setConfiguracao(null);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(PRODUTO_ACCOUNT_LEGACY, detalheProduto))
                .build();

        assertFalse(originacaoLegado.configuracaoAccountLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar false quando o produto não for ACCOUNT_LEGACY")
    void deveRetornarFalseQuandoProdutoNaoForAccountLegacy() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(CAPITAL_LEGACY, detalheProduto))
                .build();

        assertFalse(originacaoLegado.configuracaoAccountLegacyProcessado());
    }

    @Test
    @DisplayName("Deve retornar true quando tem o produto ACCOUNT_LEGACY.")
    void deveRetornarTrueQuandoTemProdutoAccountLegacy() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of(PRODUTO_ACCOUNT_LEGACY, detalheProduto, CAPITAL_LEGACY, detalheProduto))
                .build();

        assertTrue(originacaoLegado.temProduto(PRODUTO_ACCOUNT_LEGACY));
    }

    @Test
    @DisplayName("Deve retornar true quando tem o produto ACCOUNT_LEGACY.")
    void deveRetornarFalseQuandoNaoTemProdutoAccountLegacy() {
        DetalheProduto detalheProduto = Instancio.of(DetalheProduto.class).create();
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(Map.of("INVESTIMENTO_LEGACY", detalheProduto, CAPITAL_LEGACY, detalheProduto))
                .build();

        assertFalse(originacaoLegado.temProduto(PRODUTO_ACCOUNT_LEGACY));
    }

    @Test
    @DisplayName("Deve retornar true quando tem o produto ACCOUNT_LEGACY.")
    void deveRetornarFalseQuandoNaoTemNenhumProduto() {
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(null)
                .build();

        assertFalse(originacaoLegado.temProduto(PRODUTO_ACCOUNT_LEGACY));
    }

    @Test
    @DisplayName("Deve retornar true quando tem o produto algum produto legado no pedido.")
    void deveRetornarTrueQuandoNaoTemAlgumProdutoLegadoNaOriginacao() {
        var detalheProdutoLegado = Instancio.of(DetalheProduto.class).create();
        var mapDetalheProduto = new HashMap<String, DetalheProduto>();
        mapDetalheProduto.put(PRODUTO_ACCOUNT_LEGACY, detalheProdutoLegado);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(mapDetalheProduto)
                .build();

        assertTrue(originacaoLegado.temProdutoLegado());
    }

    @Test
    @DisplayName("Deve retornar false quando não tem algum produto legado no pedido.")
    void deveRetornarFalseQuandoNaoTemAlgumProdutoLegadoNaOriginacao() {
        var detalheProdutoPlataforma = Instancio.of(DetalheProduto.class).create();
        var mapDetalheProduto = new HashMap<String, DetalheProduto>();
        mapDetalheProduto.put("INDIVIDUAL_CHECKING_ACCOUNT", detalheProdutoPlataforma);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(mapDetalheProduto)
                .build();

        assertFalse(originacaoLegado.temProdutoLegado());
    }

    @Test
    @DisplayName("Deve atualizar os detalhes de um item do pedido.")
    void deveAtualizarOsDetalhesDeUmItemDoPedido() {
        var detalheProdutoAtualizado = Instancio.of(DetalheProduto.class).create();
        detalheProdutoAtualizado.setNumeroConta("123456");
        detalheProdutoAtualizado.setTipoProduto(CAPITAL_LEGACY);
        var detalheProduto = Instancio.of(DetalheProduto.class).create();
        detalheProduto.setTipoProduto(CAPITAL_LEGACY);
        var mapDetalheProduto = new HashMap<String, DetalheProduto>();
        mapDetalheProduto.put(CAPITAL_LEGACY, detalheProduto);
        OriginacaoLegado originacaoLegado = OriginacaoLegado.builder()
                .detalheProduto(mapDetalheProduto)
                .build();

        originacaoLegado.atualizarDetalheProduto(detalheProdutoAtualizado);
        assertEquals(originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY).getNumeroConta(), detalheProdutoAtualizado.getNumeroConta());
    }
}