package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class OriginacaoLegadoService {

    private final OriginacaoLegadoRepository repository;

    private final Clock clock;
    private static final String CAPITAL_COMMERCIAL_PLAN_LEGACY = "CAPITAL_COMMERCIAL_PLAN_LEGACY";
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";

    private final ProximoDiaUtilService proximoDiaUtilService;

    public LocalDate atualizarDiaPrimeiroPagamentoCapital(String idPedido) {
        log.info("[{}] - Iniciando atualização do primeiro pagamento para o produto de capital", idPedido);

        var originacaoLegado = repository.findByIdPedido(idPedido).orElseThrow(NotFoundException::new);

        var temProdutoCapitalNoPedido = originacaoLegado.temProdutoLegado() && (originacaoLegado.temProduto(CAPITAL_LEGACY) || originacaoLegado.temProduto(CAPITAL_COMMERCIAL_PLAN_LEGACY));
        if (!temProdutoCapitalNoPedido) {
            log.warn("[{}] - Não existe nenhum produdo de capital para o pedido.", idPedido);
            throw new BusinessException("Não tem nenhum produto de capital no pedido [" + idPedido + "]");
        }

        var detalheProdutoCapital = originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY);
        var diaPrimeiroPagamentoAtual = detalheProdutoCapital.getConfiguracao().getCapital().getDiaPagamento();
        var diaPrimeiroPagamentoAtualString = DataUtils.converterLocalDateParaString(diaPrimeiroPagamentoAtual);
        log.debug("[{}] - Dia do primeiro pagamento para o produto {} é [{}]", idPedido, CAPITAL_LEGACY, diaPrimeiroPagamentoAtualString);

        if(diaPrimeiroPagamentoAtual.isEqual(LocalDate.now()) || diaPrimeiroPagamentoAtual.isAfter(LocalDate.now(clock))){
            log.info("[{}] - Não foi atualizado o dia do primeiro pagamento, pois o dia do primeiro pagamento ainda não venceu o prazo. retornando o dia [{}]", idPedido, diaPrimeiroPagamentoAtualString);
            return diaPrimeiroPagamentoAtual;
        }

        var proximoDiaUtil = proximoDiaUtilService.obterPrimeiroDiaPagamento(diaPrimeiroPagamentoAtualString, originacaoLegado.getSiglaEstado(), originacaoLegado.getNomeCidade());
        log.debug("[{}] - Próximo dia útil encontrado para o produto {} é [{}]", idPedido, CAPITAL_LEGACY, diaPrimeiroPagamentoAtualString);

        detalheProdutoCapital.getConfiguracao().getCapital().setDiaPagamento(proximoDiaUtil);
        repository.save(originacaoLegado);

        log.info("[{}] - Atualização do primeiro dia de pagamento para o produto {} finalizado com sucesso. O próximo dia agora é [{}]", idPedido, CAPITAL_LEGACY, proximoDiaUtil);
        return proximoDiaUtil;
    }

}
