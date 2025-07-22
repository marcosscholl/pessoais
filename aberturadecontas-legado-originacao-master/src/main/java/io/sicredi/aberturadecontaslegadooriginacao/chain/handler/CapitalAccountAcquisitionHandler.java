package io.sicredi.aberturadecontaslegadooriginacao.chain.handler;

import io.sicredi.aberturadecontaslegadooriginacao.chain.AbstractHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import io.sicredi.aberturadecontaslegadooriginacao.exception.ConfiguracaoDetalheProdutoException;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.service.MetricasService;
import io.sicredi.aberturadecontaslegadooriginacao.service.NumeroContaService;
import io.sicredi.aberturadecontaslegadooriginacao.service.ProximoDiaUtilService;
import io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils;
import io.sicredi.capital.acquisition.grpc.AcquisitionConfigurationServiceGrpc;
import io.sicredi.capital.acquisition.grpc.ConfigurationDTO;
import io.sicredi.capital.acquisition.grpc.IdentifierDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.*;
import static io.sicredi.aberturadecontaslegadooriginacao.util.DataUtils.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class CapitalAccountAcquisitionHandler extends AbstractHandler {

    private final AcquisitionConfigurationServiceGrpc.AcquisitionConfigurationServiceBlockingStub acquisitionConfigurationServiceBlockingStub;
    private final OriginacaoLegadoMapper originacaoLegadoMapper;
    private final MetricasService metricasService;
    private final ProximoDiaUtilService proximoDiaUtilService;
    private final NumeroContaService numeroContaService;
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";
    private static final String CAPITAL_COMMERCIAL_PLAN_LEGACY = "CAPITAL_COMMERCIAL_PLAN_LEGACY";
    private static final String STATUS_PRODUTO_CANCELADO = "CANCELADO";

    @Override
    public OriginacaoLegado processarProximo(final AcquisitionEngineManagerItemsEventDTO event, final OriginacaoLegado originacaoLegado) {
        log.info("[{}] - Iniciando processamento dos produtos CAPITAL_LEGACY ou CAPITAL_COMMERCIAL_PLAN_LEGACY.", originacaoLegado.getIdPedido());
        OriginacaoLegado novaOriginacao = new OriginacaoLegado();
        try {

            if (naoTemProdutoCapitalLegacy(originacaoLegado)) {
                return originacaoLegado;
            }

            if (foiProcessado(originacaoLegado)) {
                log.info("[{}] - Produto e/ou CAPITAL_LEGACY ou CAPITAL_COMMERCIAL_PLAN_LEGACY já processado com sucesso.", originacaoLegado.getIdPedido());
                return originacaoLegado;
            }

            var listaProdutosCapital = obterListaProdutosCapital(originacaoLegado);
            novaOriginacao = originacaoLegadoMapper.merge(originacaoLegado);
            configurarProdutosCapital(listaProdutosCapital, originacaoLegado, novaOriginacao);

            if (novaOriginacao.configuracaoCapitalLegacyProcessado()) {
                novaOriginacao.removerCritica(PRODUCT_CAPITAL_LEGACY);
            }

            log.info("[{}] - Produto(s) [ CAPITAL_LEGACY ou CAPITAL_COMMERCIAL_PLAN_LEGACY ] processado(s) com sucesso.", originacaoLegado.getIdPedido());
            return novaOriginacao;
        } catch (Exception e) {
            log.error("[{}] - Erro ao buscar configuração do produto [ CAPITAL_LEGACY ] em capital-account-acquisition.", originacaoLegado.getIdPedido(), e);
            originacaoLegadoMapper.merge(originacaoLegado, novaOriginacao);
            novaOriginacao.adicionarCritica(new Critica(PRODUCT_CAPITAL_LEGACY, "Erro ao buscar configuração do produto [ CAPITAL_LEGACY ] em capital-account-acquisition para o pedido [ " + originacaoLegado.getIdPedido() + " ]", e.toString()));
            return novaOriginacao;
        }
    }

    private boolean naoTemProdutoCapitalLegacy(final OriginacaoLegado originacaoLegado) {
        var naoTemProdutoCapitalLegacy = obterListaProdutosCapital(originacaoLegado).isEmpty();
        if (Boolean.TRUE.equals(naoTemProdutoCapitalLegacy)) {
            log.info("[{}] - Não tem nenhum produto [ {} ou {} ] no pedido para ser configurado.", originacaoLegado.getIdPedido(), CAPITAL_LEGACY, CAPITAL_COMMERCIAL_PLAN_LEGACY);
        }
        return naoTemProdutoCapitalLegacy;
    }

    private boolean foiProcessado(final OriginacaoLegado originacaoLegado) {
        return !originacaoLegado.temCritica(PRODUCT_CAPITAL_LEGACY) && Boolean.TRUE.equals(originacaoLegado.configuracaoCapitalLegacyProcessado());
    }

    private List<Map.Entry<String, DetalheProduto>> obterListaProdutosCapital(final OriginacaoLegado originacaoLegado) {
        return originacaoLegado.getDetalheProduto().entrySet().stream()
                .filter(produto -> CAPITAL_LEGACY.equals(produto.getKey()) || CAPITAL_COMMERCIAL_PLAN_LEGACY.equals(produto.getKey()))
                .sorted(Comparator.comparing(entry -> {
                    if (CAPITAL_LEGACY.equals(entry.getKey())) return 0;
                    else return 1;
                }))
                .toList();
    }

    private void configurarProdutosCapital(final List<Map.Entry<String, DetalheProduto>> listaProdutosCapital, final OriginacaoLegado originacaoLegado, OriginacaoLegado novaOriginacao) {
        listaProdutosCapital.forEach(detalheProduto -> {
            if (STATUS_PRODUTO_CANCELADO.equals(detalheProduto.getValue().getStatus())) {
                log.info("[{}] - Produto [ {} ] não processado pois está CANCELADO.", detalheProduto.getValue().getIdItemPedido(), detalheProduto.getValue().getTipoProduto());
                return;
            }

            var request = IdentifierDTO.newBuilder().setId(detalheProduto.getValue().getIdSimulacao()).build();
            ConfigurationDTO configuracao = acquisitionConfigurationServiceBlockingStub.getConfiguration(request);

            log.debug("[{}] - Configuração do produto para o pedido retornado com sucesso. {}", originacaoLegado.getIdPedido(), configuracao);
            if (CAPITAL_LEGACY.equals(detalheProduto.getKey())) {
                var numeroConta = numeroContaService.obterNumeroConta(novaOriginacao);
                var itemPedido = novaOriginacao.getDetalheProduto().get(detalheProduto.getKey());
                itemPedido.setNumeroConta(numeroConta);
            }

            if (configuracao != null) {
                ConfiguracaoDetalhe configuracaoPlano = configurarDetalhePlano(originacaoLegado, detalheProduto, configuracao);
                var itemPedido = novaOriginacao.getDetalheProduto().get(detalheProduto.getKey());
                itemPedido.setConfiguracao(configuracaoPlano);
            } else {
                String mensagemLog = "Não foi encontrado nenhuma configuração de produto [ " + detalheProduto.getKey() + " ] em capital-account-acquisition para o pedido [ " + originacaoLegado.getIdPedido() + " ]";
                novaOriginacao.adicionarCritica(new Critica(PRODUCT_CAPITAL_LEGACY, mensagemLog));
                log.warn(mensagemLog);
            }

            log.debug("[{}] - Configuração do [ {} ] produto recuperado e processado com sucesso.", detalheProduto.getValue().getIdSimulacao(), detalheProduto.getKey());
        });
    }

    private ConfiguracaoDetalhe configurarDetalhePlano(OriginacaoLegado originacaoLegado, Map.Entry<String, DetalheProduto> item, ConfigurationDTO configuracao) {
        ConfiguracaoDetalhe configuracaoPlano = new ConfiguracaoDetalhe();
        boolean produtoComParcelamento = false;
        boolean produtoComPlanoCooperativista = false;

        try {
            String dataPrimeiroPagamentoConfiguracao = obterDataPrimeiroPagamento(configuracao);
            LocalDate primeiroDiaPagamento = proximoDiaUtilService.obterPrimeiroDiaPagamento(dataPrimeiroPagamentoConfiguracao, originacaoLegado.getSiglaEstado(), originacaoLegado.getNomeCidade());
            configuracaoPlano.setCapital(new Capital(configuracao.getAmount(), primeiroDiaPagamento));

            if (CAPITAL_LEGACY.equals(item.getKey())) {
                produtoComParcelamento = configurarPlanoEstatutario(configuracao, configuracaoPlano, primeiroDiaPagamento);
                registrarMetricaParcelamento(produtoComParcelamento);
            } else {
                produtoComPlanoCooperativista = true;
                configurarPlanoCooperativista(originacaoLegado, configuracao, configuracaoPlano);
            }

            return configuracaoPlano;

        } catch (Exception e) {
            registrarMetricaErro(produtoComParcelamento, produtoComPlanoCooperativista);
            throw new ConfiguracaoDetalheProdutoException(e);
        }
    }

    private String obterDataPrimeiroPagamento(ConfigurationDTO configuracao) {
        return StringUtils.isNotBlank(configuracao.getScheduleDate())
                ? configuracao.getScheduleDate()
                : configuracao.getPaymentScheduleConfig().getFirstPaymentDate();
    }

    private boolean configurarPlanoEstatutario(ConfigurationDTO configuracao, ConfiguracaoDetalhe configuracaoPlano, LocalDate primeiroDiaPagamento) {
        if (!planoEstatutario(configuracao)) return false;

        var planos = dividirPlano(
                configuracao.getPaymentScheduleConfig().getInstallments(),
                configuracao.getPaymentScheduleConfig().getMonthlyAmount(),
                configuracao.getPaymentScheduleConfig().getFirstPaymentDate(),
                primeiroDiaPagamento
        );
        configuracaoPlano.getCapital().setPlanos(planos);
        return true;
    }

    private void registrarMetricaParcelamento(boolean comParcelamento) {
        String metrica = comParcelamento
                ? "event_produto_capital_legacy_com_parcelamento_sucesso"
                : "event_produto_capital_legacy_sem_parcelamento_sucesso";
        metricasService.incrementCounter(metrica);
    }

    private void configurarPlanoCooperativista(OriginacaoLegado originacaoLegado, ConfigurationDTO configuracao, ConfiguracaoDetalhe configuracaoPlano) {
        if (Objects.isNull(originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY))) return;

        var planos = dividirPlano(
                configuracao.getPaymentScheduleConfig().getInstallments(),
                configuracao.getPaymentScheduleConfig().getMonthlyAmount(),
                configuracao.getPaymentScheduleConfig().getFirstPaymentDate(),
                null
        );

        var capital = originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital();
        var diaCapitalInicial = capital.getDiaPagamento();
        var planoEstatutario = capital.getPlanos();
        var diaPrimeiroPagamentoEstatutario = !CollectionUtils.isEmpty(planoEstatutario) ? planoEstatutario.getFirst().getDiaPrimeiroPagamento() : null;
        var diaPrimeiroPagamentoCooperativista = StringUtils.isNotBlank(configuracao.getPaymentScheduleConfig().getFirstPaymentDate())
                ? DataUtils.converterStringToLocalDate(configuracao.getPaymentScheduleConfig().getFirstPaymentDate())
                : null;

        var datasAjustadas = ajustarDiasDuplicados(Arrays.asList(diaCapitalInicial, diaPrimeiroPagamentoEstatutario, diaPrimeiroPagamentoCooperativista));

        originacaoLegado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().setDiaPagamento(datasAjustadas.get(0));
        if (diaPrimeiroPagamentoEstatutario != null) {
            planoEstatutario.getFirst().setDiaPrimeiroPagamento(datasAjustadas.get(1));
            var dataUltimoPagamento = planoEstatutario.getFirst().getDiaUltimoPagamento();
            planoEstatutario.getFirst().setDiaUltimoPagamento(ajustarDia(datasAjustadas.get(1), dataUltimoPagamento));
        }
        if (diaPrimeiroPagamentoCooperativista != null) {
            planos.getFirst().setDiaPrimeiroPagamento(datasAjustadas.get(2));
            var dataUltimoPagamento = planos.getFirst().getDiaUltimoPagamento();
            planos.getFirst().setDiaUltimoPagamento(ajustarDia(datasAjustadas.get(2), dataUltimoPagamento));
        }

        configuracaoPlano.setCapital(new Capital(planos));
        metricasService.incrementCounter("event_produto_capital_legacy_plano_cooperativista_sucesso");
    }

    private void registrarMetricaErro(boolean comParcelamento, boolean comPlanoCooperativista) {
        if (comParcelamento) {
            metricasService.incrementCounter("event_produto_capital_legacy_com_parcelamento_falha");
        } else if (comPlanoCooperativista) {
            metricasService.incrementCounter("event_produto_capital_legacy_plano_cooperativista_falha");
        }
    }

    private List<LocalDate> ajustarDiasDuplicados(Collection<LocalDate> datas) {
        Set<Integer> diasUsados = new HashSet<>(datas.size());
        List<LocalDate> resultado = new ArrayList<>(datas.size());

        for (LocalDate data : datas) {
            if (data == null) {
                resultado.add(null);
                continue;
            }

            LocalDate ajustada = data;
            while (diasUsados.contains(ajustada.getDayOfMonth())) {
                ajustada = ajustada.plusDays(1);
            }

            diasUsados.add(ajustada.getDayOfMonth());
            resultado.add(ajustada);
        }

        return resultado;
    }

    private boolean planoEstatutario(final ConfigurationDTO configuracao) {
        return !configuracao.getPaymentScheduleConfig().getFirstPaymentDate().isBlank();
    }

    public List<Plano> dividirPlano(final int totalParcelas, final double valorParcela, final String dataInicio, final LocalDate diaPagamentoCapitalInicial) {
        var dataInicioPlano = converterStringToLocalDate(dataInicio);

        if (Objects.nonNull(diaPagamentoCapitalInicial) && diaPagamentoCapitalInicial.getDayOfMonth() == dataInicioPlano.getDayOfMonth()) {
            dataInicioPlano = dataInicioPlano.plusDays(1);
        }

        List<Plano> planos = new ArrayList<>(0);
        int maxParcelasPorPlano = 99;
        int parcelasRestantes = totalParcelas;
        LocalDate inicioPlano = null;

        while (parcelasRestantes > 0) {
            int parcelasNoPlano = Math.min(maxParcelasPorPlano, parcelasRestantes);

            if (inicioPlano == null) {
                inicioPlano = dataInicioPlano;
            }
            LocalDate dataFinal = ajustarDia(inicioPlano, inicioPlano.plusMonths(parcelasNoPlano - 1L));

            planos.add(new Plano(valorParcela, parcelasNoPlano, inicioPlano, dataFinal));
            parcelasRestantes -= parcelasNoPlano;
            inicioPlano = dataFinal.plusMonths(1);
        }

        return planos;
    }

    private LocalDate ajustarDia(LocalDate dataReferencia, LocalDate dataDestino) {
        if (dataReferencia == null || dataDestino == null) {
            throw new IllegalArgumentException("Data de início e fim do plano não pode ser nula.");
        }

        long diferencaEntreDatas = Math.abs(dataReferencia.getDayOfMonth() - dataDestino.getDayOfMonth());
        if (diferencaEntreDatas > 0) {
            return dataDestino.plusDays(diferencaEntreDatas);
        }

        return dataDestino;
    }
}