package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.service.NumeroContaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.PRODUCT_INVESTMENT_LEGACY;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvestimentHandler extends AbstractHandler {

    private final OriginacaoLegadoMapper originacaoLegadoMapper;
    private final NumeroContaService numeroContaService;
    private static final String POUPANCA = "INVESTMENT_LEGACY";
    private static final String CONTA_CORRENTE = "ACCOUNT_LEGACY";

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando processamento do produto [ {} ].", originacaoLegado.getIdPedido(), POUPANCA);
        var novoProdutoDaOriginacao = new OriginacaoLegado();
        try {
            if (naoTemProdutoInvestimentLegacy(originacaoLegado)) {
                log.info("[{}] - Não existe nenhum produto [ {} ] no pedido para ser processado.", originacaoLegado.getIdPedido(), POUPANCA);
                return originacaoLegado;
            }

            novoProdutoDaOriginacao = originacaoLegadoMapper.merge(originacaoLegado);

            String numeroContaPoupanca = numeroContaService.obterNumeroConta(novoProdutoDaOriginacao);
            novoProdutoDaOriginacao.getDetalheProduto().get(POUPANCA).setNumeroConta(numeroContaPoupanca);
            novoProdutoDaOriginacao.removerCritica(PRODUCT_INVESTMENT_LEGACY);
            log.info("[{}] - Produto [ {} ] processado com sucesso.", originacaoLegado.getIdPedido(), POUPANCA);
            return novoProdutoDaOriginacao;

        } catch (Exception e) {
            log.error("[{}] - Erro ao processar o produto [ {} ].", originacaoLegado.getIdPedido(), POUPANCA, e);
            novoProdutoDaOriginacao.adicionarCritica(new Critica(PRODUCT_INVESTMENT_LEGACY, "Erro ao processar os dados do produto [ " + POUPANCA + " ] para o pedido [ " + originacaoLegado.getIdPedido() + " ]", e.getMessage()));
            return novoProdutoDaOriginacao;
        }
    }

    private boolean naoTemProdutoInvestimentLegacy(final OriginacaoLegado originacaoLegado) {
        return !originacaoLegado.temProduto(POUPANCA);
    }

    private String obterNumeroContaPoupanca(final OriginacaoLegado originacaoLegado) {
        log.debug("[{}] - Iniciando obtenção do número da conta poupança.", originacaoLegado.getIdPedido());
        if (pedidoPossuiProdutoContaCorrente(originacaoLegado)) {
            var numeroConta = originacaoLegado.getDetalheProduto().get(CONTA_CORRENTE).getNumeroConta();
            if(StringUtils.isBlank(numeroConta)){
              throw new NotFoundException("Não foi encontrado o número da conta corrente para ser adicionado a conta poupança.");
            }
            log.debug("[{}] - O numero de conta poupança [ {} ] foi adicionado com sucesso.", originacaoLegado.getIdPedido(), numeroConta);
            return numeroConta;
        }
        log.debug("[{}] - Não foi encontrado o número da conta poupança, pois o pedido não possui nenhum produto de conta corrente.", originacaoLegado.getIdPedido());
        return null;
    }

    private boolean pedidoPossuiProdutoContaCorrente(final OriginacaoLegado originacaoLegado) {
        return Objects.nonNull(originacaoLegado.getDetalheProduto().get(CONTA_CORRENTE));
    }
}
