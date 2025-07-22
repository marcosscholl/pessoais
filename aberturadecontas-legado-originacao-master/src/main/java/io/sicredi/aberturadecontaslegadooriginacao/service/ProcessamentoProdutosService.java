package io.sicredi.aberturadecontaslegadooriginacao.service;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsCreationInputDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsDetailsInputDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DetalhesPedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.DetalheProduto;
import io.sicredi.aberturadecontaslegadooriginacao.entities.OriginacaoLegado;
import io.sicredi.aberturadecontaslegadooriginacao.event.AcquisitionProductsCreationInputProducer;
import io.sicredi.aberturadecontaslegadooriginacao.event.AcquisitionProductsDetailsInputProducer;
import io.sicredi.aberturadecontaslegadooriginacao.repository.OriginacaoLegadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProcessamentoProdutosService {

    private static final String LIBERADO = "LIBERADO";
    private static final String CANCELADO = "CANCELADO";
    public static final String ERRO_EFETIVACAO = "ERRO_EFETIVACAO";
    public static final String CANCELADO_POR = "SYSTEM";
    public static final String PERIGO = "danger";
    public static final String ITEM_CANCELADO = "Item cancelado";
    public static final String EVENT_SOLICITACAO_CANCELAMENTO_ITEM_PEDIDO = "event_solicitacao_cancelamento_item_pedido";
    public static final String EVENT_CANCELAMENTO_ITEM_PEDIDO = "event_cancelamento_item_pedido";
    public static final String EVENT_EFETIVACAO_ITEM_PEDIDO = "event_efetivacao_item_pedido";
    public static final String EVENT_FALHA_EFETIVAVAO = "event_falha_efetivacao";
    public static final String MENSAGEM_PADRAO = "Não foi possível contratar o produto no Sistema Legado.";
    public static final String DESCRICAO_ERRO_PADRAO = "Erro ao efetivar o produto.";
    public static final String FINALIZADO = "FINALIZADO";
    public static final String FALHA = "FALHA";
    public static final String CAPITAL_COMMERCIAL_PLAN_LEGACY = "CAPITAL_COMMERCIAL_PLAN_LEGACY";

    private final AcquisitionProductsCreationInputProducer acquisitionProductsCreationInputProducer;
    private final AcquisitionProductsDetailsInputProducer acquisitionProductsDetailsInputProducer;
    private final MetricasService metricasService;
    private final OriginacaoLegadoRepository repository;

    @Value("${sicredi.aberturadecontas-legado-originacao.cancelamentoAutomaticoItemPedidoHabilitado}")
    private final Boolean cancelamentoAutomaticoItemPedidoHabilitado;

    public OriginacaoLegado processarStatusPedido(String idPedido, String idItemPedido, String status, DetalhesPedidoDTO detalheCancelamento) {
        try {
            log.info("[{}] Iniciando processamento status item pedido. status: {}, idItemPedido: {}", idPedido, status, idItemPedido);

            OriginacaoLegado originacaoLegado = this.buscarOriginacaoLegadoPorIdPedido(idPedido);
            if (CANCELADO.equalsIgnoreCase(originacaoLegado.getStatus()) || FINALIZADO.equalsIgnoreCase(originacaoLegado.getStatus())) {
                log.info("[{}] Todos os itens do pedido foram processados. situação atual do pedido: {}", idPedido, originacaoLegado.getStatus());
                return originacaoLegado;
            }

            List<DetalheProduto> detalheProdutos = this.buscarProdutos(originacaoLegado);
            DetalheProduto itemPedido = this.buscarItemPedido(detalheProdutos, idItemPedido);

            if (FALHA.equalsIgnoreCase(status)) {
                originacaoLegado.setStatus(FALHA);
                originacaoLegado.getDetalheProduto().get(itemPedido.getTipoProduto()).setMensagemDetalhe(detalheCancelamento.descricaoErro());
                var originacaoAtualizada = repository.save(originacaoLegado);
                metricasService.incrementCounter(EVENT_FALHA_EFETIVAVAO);
                return originacaoAtualizada;
            }

            if (!CAPITAL_COMMERCIAL_PLAN_LEGACY.equalsIgnoreCase(itemPedido.getTipoProduto())
                && Objects.nonNull(detalheCancelamento.numeroConta())
                && (Objects.isNull(itemPedido.getNumeroConta()) || !itemPedido.getNumeroConta().equals(detalheCancelamento.numeroConta()))) {

                log.info("[{}] O número da conta [ {} ] é diferente do valor recebido do BPEL [ {} ]. Atualizando na base de dados o item [ {} ] com o valor recebido do BPEL.",
                        idPedido, itemPedido.getNumeroConta(), detalheCancelamento.numeroConta(), idItemPedido);

                itemPedido.setNumeroConta(detalheCancelamento.numeroConta());
                originacaoLegado.atualizarDetalheProduto(itemPedido);
            }

            if (LIBERADO.equalsIgnoreCase(status)) {
                this.efetivarItemPedido(idPedido, idItemPedido);
                originacaoLegado.getDetalheProduto().get(itemPedido.getTipoProduto()).setStatus(LIBERADO);
                originacaoLegado.getDetalheProduto().get(itemPedido.getTipoProduto()).setMensagemDetalhe(detalheCancelamento.mensagem());
            } else if (CANCELADO.equalsIgnoreCase(status)) {
                this.cancelarItemPedidoAutomatico(idPedido, idItemPedido, detalheCancelamento, originacaoLegado, itemPedido.getTipoProduto());
            }

            this.verificarProcessamentoProdutos(originacaoLegado);

            OriginacaoLegado originacaoLegadoAtualizada = repository.save(originacaoLegado);

            log.info("[{}] Processamento status item pedido finalizado com sucesso. idItemPedido: {}", idPedido, idItemPedido);

            return originacaoLegadoAtualizada;
        } catch (Exception e) {
            log.error("[{}] - Erro ao tentar atualizar o status do item [{}].", idPedido, idItemPedido, e);
            throw new BusinessException(e);
        }
    }

    public OriginacaoLegado cancelarItemPedidoManual(String idPedido, String idItemPedido, DetalhesPedidoDTO detalheCancelamento) {
        log.info("[{}] Iniciando cancelamento manual para o item do pedido. idItemPedido: {}", idPedido, idItemPedido);

        OriginacaoLegado originacaoLegado = this.buscarOriginacaoLegadoPorIdPedido(idPedido);
        if (CANCELADO.equalsIgnoreCase(originacaoLegado.getStatus()) || FINALIZADO.equalsIgnoreCase(originacaoLegado.getStatus())) {
            log.info("[{}] Todos os itens do produto foram processados. situação atual do pedido: {}", idPedido, originacaoLegado.getStatus());
            return originacaoLegado;
        }

        List<DetalheProduto> detalheProdutos = this.buscarProdutos(originacaoLegado);
        DetalheProduto itemPedido = this.buscarItemPedido(detalheProdutos, idItemPedido);

        this.cancelarItemPedido(idPedido, idItemPedido, detalheCancelamento);
        originacaoLegado.getDetalheProduto().get(itemPedido.getTipoProduto()).setStatus(CANCELADO);

        this.verificarProcessamentoProdutos(originacaoLegado);
        OriginacaoLegado originacaoLegadoAtualizada = repository.save(originacaoLegado);

        log.info("[{}] Cancelamento manual do item pedido finalizado com sucesso. idItemPedido: {}", idPedido, idItemPedido);
        return originacaoLegadoAtualizada;
    }

    private void verificarProcessamentoProdutos(OriginacaoLegado originacaoLegado) {
        if (Boolean.TRUE.equals(this.validarTodosOsProdutosForamProcessados(originacaoLegado))) {
            if (Boolean.TRUE.equals(this.validarTodosOsProdutosCancelados(originacaoLegado)))
                originacaoLegado.setStatus(CANCELADO);
            else
                originacaoLegado.setStatus(FINALIZADO);
        }
    }

    private Boolean validarTodosOsProdutosCancelados(OriginacaoLegado originacaoLegado) {
        return this.buscarProdutos(originacaoLegado).stream().allMatch(it -> CANCELADO.equalsIgnoreCase(it.getStatus()));
    }

    private Boolean validarTodosOsProdutosForamProcessados(OriginacaoLegado originacaoLegado) {
        return this.buscarProdutos(originacaoLegado).stream().allMatch(it -> LIBERADO.equalsIgnoreCase(it.getStatus()) || CANCELADO.equalsIgnoreCase(it.getStatus()));
    }

    private OriginacaoLegado buscarOriginacaoLegadoPorIdPedido(String idPedido) {
        return repository.findByIdPedido(idPedido)
                .orElseThrow(() -> new NotFoundException("Não foi possivel encontrar pedido com idPedido " + idPedido));
    }

    private DetalheProduto buscarItemPedido(Collection<DetalheProduto> detalheProdutos, String idItemPedido) {
        return detalheProdutos
                .stream()
                .filter(it -> idItemPedido.equalsIgnoreCase(it.getIdItemPedido()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Não foi possivel encontrar o item do pedido com idItemPedido " + idItemPedido));
    }

    private List<DetalheProduto> buscarProdutos(OriginacaoLegado originacaoLegado) {
        return originacaoLegado.getDetalheProduto().values().stream().toList();
    }

    private void efetivarItemPedido(String idPedido, String idItemPedido) {
        metricasService.incrementCounter(EVENT_EFETIVACAO_ITEM_PEDIDO);
        acquisitionProductsCreationInputProducer.send(AcquisitionProductsCreationInputDTO.builder()
                .idPedido(idPedido)
                .idProdutoPedido(idItemPedido)
                .dataCriacao(LocalDate.now())
                .build());
    }

    private void cancelarItemPedidoAutomatico(String idPedido, String idItemPedido, DetalhesPedidoDTO detalheCancelamento, OriginacaoLegado originacaoLegado, String tipoProduto) {
        metricasService.incrementCounter(EVENT_SOLICITACAO_CANCELAMENTO_ITEM_PEDIDO);
        if (Boolean.TRUE.equals(cancelamentoAutomaticoItemPedidoHabilitado)) {
            DetalhesPedidoDTO detalheError = Objects.requireNonNullElse(detalheCancelamento,
                    DetalhesPedidoDTO.builder()
                            .mensagem(MENSAGEM_PADRAO)
                            .descricaoErro(DESCRICAO_ERRO_PADRAO)
                            .build());
            this.cancelarItemPedido(idPedido, idItemPedido, detalheError);
            originacaoLegado.getDetalheProduto().get(tipoProduto).setStatus(CANCELADO);
            originacaoLegado.getDetalheProduto().get(tipoProduto).setMensagemDetalhe(detalheCancelamento.descricaoErro());
        } else
            log.info("[{}] Solicitação de cancelamento do item {}. Item deve ser cancelado manualmente.", idPedido, idItemPedido);
    }

    private void cancelarItemPedido(String idPedido, String idItemPedido, DetalhesPedidoDTO detalhesPedidoDTO) {
        metricasService.incrementCounter(EVENT_CANCELAMENTO_ITEM_PEDIDO);
        AcquisitionProductsDetailsInputDTO mensagem = AcquisitionProductsDetailsInputDTO.builder()
                .idPedido(idPedido)
                .idProdutoPedido(idItemPedido)
                .podeSerCancelado(Boolean.TRUE)
                .status(AcquisitionProductsDetailsInputDTO.Info.builder()
                        .texto(ITEM_CANCELADO)
                        .aparencia(PERIGO)
                        .build())
                .info(List.of(AcquisitionProductsDetailsInputDTO.Info.builder()
                        .texto(Objects.requireNonNullElse(detalhesPedidoDTO.mensagem(), MENSAGEM_PADRAO))
                        .aparencia(PERIGO)
                        .build()))
                .cancelar(ERRO_EFETIVACAO)
                .infoCancelamento(AcquisitionProductsDetailsInputDTO.CancelInfo.builder()
                        .canceladoPor(CANCELADO_POR)
                        .motivo(ERRO_EFETIVACAO)
                        .descricao(Objects.requireNonNullElse(detalhesPedidoDTO.mensagem(), DESCRICAO_ERRO_PADRAO))
                        .build())
                .build();

        acquisitionProductsDetailsInputProducer.send(mensagem);
    }
}
