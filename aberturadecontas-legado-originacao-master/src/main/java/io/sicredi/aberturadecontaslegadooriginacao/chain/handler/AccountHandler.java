package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionCheckingAccountClient;
import io.sicredi.aberturadecontaslegadooriginacao.client.MonthlyFeeSimulationClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DadosSimulacaoCestaRelacionamentoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.NumeroContaDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class AccountHandler extends AbstractHandler {

    private final MonthlyFeeSimulationClient monthlyFeeSimulationClient;
    private final AcquisitionCheckingAccountClient acquisitionCheckingAccountClient;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;
    private static final String ACCOUNT_LEGACY = "ACCOUNT_LEGACY";

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando configuração do produto ACCOUNT_LEGACY.", originacaoLegado.getIdPedido());
        var novoProdutoDaOriginacao = new OriginacaoLegado();
        try {

            if(naoTemProdutoAccountLegacy(originacaoLegado)){
                log.info("[{}] - Não existem nenhum item no pedido para o produto ACCOUNT_LEGACY.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            if (foiProcessado(originacaoLegado)) {
                log.info("[{}] - O produto ACCOUNT_LEGACY já foi processado.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            novoProdutoDaOriginacao = originacaoLegadoMapper.merge(originacaoLegado);

            processarDetalheProdutoDaOriginacao(novoProdutoDaOriginacao);
            if (Boolean.TRUE.equals(novoProdutoDaOriginacao.configuracaoAccountLegacyProcessado())) {
                novoProdutoDaOriginacao.removerCritica(PRODUCT_ACCOUNT_LEGACY);
            }

            log.info("[{}] - Produto [ {} ] configurado e processado com sucesso . {}", originacaoLegado.getIdPedido(), ACCOUNT_LEGACY, novoProdutoDaOriginacao);
            return novoProdutoDaOriginacao;

        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar pedido no acquisition-orders-v1. error: {}", originacaoLegado.getIdPedido(), e.getMessage());
            return adicionarCritica(novoProdutoDaOriginacao, e.getMessage());
        }
    }

    private boolean naoTemProdutoAccountLegacy(final OriginacaoLegado originacaoLegado){
        return !originacaoLegado.temProduto(ACCOUNT_LEGACY);
    }

    private boolean foiProcessado(final OriginacaoLegado originacaoLegado) {
        return !originacaoLegado.temCritica(PRODUCT_ACCOUNT_LEGACY) && originacaoLegado.configuracaoAccountLegacyProcessado();
    }

    private OriginacaoLegado adicionarCritica(OriginacaoLegado originacaoLegado, String detalhe) {
        originacaoLegado.adicionarCritica(new Critica(PRODUCT_ACCOUNT_LEGACY, "Erro ao processar os dados do produto [ " + ACCOUNT_LEGACY + " ] para o pedido [ " + originacaoLegado.getIdPedido() + " ]", detalhe));
        return originacaoLegado;
    }

    private void processarDetalheProdutoDaOriginacao(OriginacaoLegado originacaoLegado) {
        log.debug("[{}] - Iniciando configuração do produto [ {} ]  ", originacaoLegado.getIdPedido(), ACCOUNT_LEGACY);

        var detalheAccountLegacy = obterDetalhesProduto(originacaoLegado).orElseThrow(NotFoundException::new);
        log.debug("[{}] - Detalhes do produto [ {} ] retornados com sucesso: {} ", originacaoLegado.getIdPedido(), ACCOUNT_LEGACY, detalheAccountLegacy);

        var dadosCestaRelacionamento = monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(detalheAccountLegacy.getIdSimulacao());
        log.debug("[{}] - Dados da cesta de relacionamento para o produto[ {} ] para o pedido retornado com sucesso. {}", originacaoLegado.getIdPedido(), ACCOUNT_LEGACY, dadosCestaRelacionamento);

        var dadosConta = acquisitionCheckingAccountClient.buscarNumeroConta(originacaoLegado.getCooperativa(), originacaoLegado.getIdPedido());
        log.debug("[{}] - Dados do número de conta para o produto[ {} ] para o pedido retornado com sucesso. {}", originacaoLegado.getIdPedido(), ACCOUNT_LEGACY, dadosConta);

        if (temInformacoesBasicasParaConfigurarProduto(dadosCestaRelacionamento, dadosConta)) {
            preencherDetalhesProduto(detalheAccountLegacy, dadosCestaRelacionamento, dadosConta);
            originacaoLegado.getDetalheProduto().get(ACCOUNT_LEGACY).setConfiguracao(detalheAccountLegacy.getConfiguracao());
        } else {
            adicionarCriticaConfiguracaoNaoEncontrada(originacaoLegado);
        }

        log.debug("[{}] - Configuração do [ {} ] configurado com sucesso.", detalheAccountLegacy.getIdSimulacao(), ACCOUNT_LEGACY);
    }

    private boolean temInformacoesBasicasParaConfigurarProduto(final DadosSimulacaoCestaRelacionamentoDTO dadosCestaRelacionamento, final NumeroContaDTO dadosConta) {
        return Objects.nonNull(dadosCestaRelacionamento) && Objects.nonNull(dadosConta);
    }

    private void preencherDetalhesProduto(final DetalheProduto detalheAccountLegacy, final DadosSimulacaoCestaRelacionamentoDTO dadosCestaRelacionamento, final NumeroContaDTO dadosConta) {
        ConfiguracaoDetalhe configuracaoDetalhe = new ConfiguracaoDetalhe();
        configuracaoDetalhe.setCestaRelacionamento(new CestaRelacionamento(dadosCestaRelacionamento.id(), formatarDiaPagamento(dadosCestaRelacionamento.diaPagamento())));
        detalheAccountLegacy.setNumeroConta(dadosConta.numerConta());
        detalheAccountLegacy.setConfiguracao(configuracaoDetalhe);
    }

    private void adicionarCriticaConfiguracaoNaoEncontrada(OriginacaoLegado novaOriginacaoLegadoComDetalhesProduto) {
        String mensagemLog = "Não foi encontrado nenhuma configuração de produto [ " + ACCOUNT_LEGACY + " ] em capital-account-acquisition para o pedido [ " + novaOriginacaoLegadoComDetalhesProduto.getIdPedido() + " ]";
        novaOriginacaoLegadoComDetalhesProduto.adicionarCritica(new Critica(PRODUCT_ACCOUNT_LEGACY, mensagemLog, "Não foi possível carregar todos os dados minimos para configuração do produto ACCOUNT_LEGACY."));
        log.debug(mensagemLog);
    }

    private Optional<DetalheProduto> obterDetalhesProduto(final OriginacaoLegado originacaoLegado) {
        return Optional.ofNullable(originacaoLegado.getDetalheProduto().get(ACCOUNT_LEGACY));
    }

    private String formatarDiaPagamento(final Integer diaPagamento){
        DecimalFormat formatoDiaPagamento = new DecimalFormat("00");
        return formatoDiaPagamento.format(diaPagamento);
    }
}
