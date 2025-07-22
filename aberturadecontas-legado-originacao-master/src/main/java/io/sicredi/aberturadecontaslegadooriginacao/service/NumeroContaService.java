package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.client.AcquisitionCheckingAccountClient;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class NumeroContaService {

    private final AcquisitionCheckingAccountClient acquisitionCheckingAccountClient;

    private static final String CONTA_CORRENTE = "ACCOUNT_LEGACY";

    public String obterNumeroConta(final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando obtenção do número de conta no serviço checking-account.", originacaoLegado.getIdPedido());
        try {
            if (pedidoPossuiProdutoContaCorrente(originacaoLegado)) {
                return obterNumeroContaCorrente(originacaoLegado);
            }
            var dadosConta = acquisitionCheckingAccountClient.buscarNumeroConta(originacaoLegado.getCooperativa(), originacaoLegado.getIdPedido());
            log.debug("[{}] - Dados do número de conta para o produto[ CAPITAL_LEGACY ] para o pedido retornado com sucesso. {}", originacaoLegado.getIdPedido(), dadosConta);

            log.info("[{}] - Numeração de conta retornado com sucesso do serviço checking-account.", originacaoLegado.getIdPedido());
            return dadosConta.numerConta();
        } catch (Exception ex) {
            log.error("Erro ao tentar recuperar o número de conta no serviço checking-account.", ex);
            return null;
        }
    }

    private String obterNumeroContaCorrente(final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando obtenção do número da conta.", originacaoLegado.getIdPedido());

        var numeroConta = originacaoLegado.getDetalheProduto().get(CONTA_CORRENTE).getNumeroConta();

        if (StringUtils.isBlank(numeroConta)) {
            log.warn("[{}] - Não existe número de conta no produto de conta corrente.", originacaoLegado.getIdPedido());
            throw new NotFoundException("Não foi encontrado o número da conta corrente.");
        }

        log.info("[{}] - O número de conta [ {} ] recuperado com sucesso.", originacaoLegado.getIdPedido(), numeroConta);
        return numeroConta;
    }

    private boolean pedidoPossuiProdutoContaCorrente(final OriginacaoLegado originacaoLegado) {
        return Objects.nonNull(originacaoLegado.getDetalheProduto().get(CONTA_CORRENTE));
    }
}
