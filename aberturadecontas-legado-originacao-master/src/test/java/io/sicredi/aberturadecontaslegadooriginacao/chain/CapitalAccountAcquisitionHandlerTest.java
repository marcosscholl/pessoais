package io.sicredi.aberturadecontaslegadooriginacao.chain;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtil;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.CapitalAccountAcquisitionHandler;
import io.sicredi.aberturadecontaslegadooriginacao.client.AdminServiceSOAPClient;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.entities.*;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import io.sicredi.aberturadecontaslegadooriginacao.mapper.OriginacaoLegadoMapper;
import io.sicredi.aberturadecontaslegadooriginacao.service.MetricasService;
import io.sicredi.aberturadecontaslegadooriginacao.service.NumeroContaService;
import io.sicredi.aberturadecontaslegadooriginacao.service.ProximoDiaUtilService;
import io.sicredi.capital.acquisition.grpc.AcquisitionConfigurationServiceGrpc;
import io.sicredi.capital.acquisition.grpc.ConfigurationDTO;
import io.sicredi.capital.acquisition.grpc.IdentifierDTO;
import io.sicredi.capital.acquisition.grpc.PaymentScheduleConfig;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.sicredi.aberturadecontaslegadooriginacao.entities.EtapaProcessoOriginacao.PRODUCT_CAPITAL_LEGACY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CapitalAccountAcquisitionHandlerTest {

    @Mock
    private AcquisitionConfigurationServiceGrpc.AcquisitionConfigurationServiceBlockingStub acquisitionConfigurationServiceBlockingStub;

    @InjectMocks
    CapitalAccountAcquisitionHandler capitalAccountAcquisitionHandler;

    @Mock
    private OriginacaoLegadoMapper originacaoLegadoMapper;

    @Mock
    private AdminServiceSOAPClient adminServiceSOAPClient;

    @Mock
    private ProximoDiaUtilService proximoDiaUtilService;

    @Mock
    private NumeroContaService numeroContaService;

    @Mock
    MetricasService metricasService;

    private static final String ID_PEDIDO = "idPedido";
    private static final String ID_COOPERATIVA = "testCoopId";
    private static final String CPF = "testSuid";
    private static final String DIA_PAGAMENTO = "2025-03-31";
    private static final String AGENDAMENTO_CONTA = "testAccount";
    private static final String ID_SIMULACAO = "1";
    private static final String ID_SIMULACAO_2 = "2";
    private static final Double VALOR = 100.0;
    private static final String CAPITAL_LEGACY = "CAPITAL_LEGACY";
    private static final String MENSAGEM_ERRO = "Erro";
    private static final String CAPITAL_COMMERCIAL_PLAN_LEGACY = "CAPITAL_COMMERCIAL_PLAN_LEGACY";
    private static final String MASCARA_DATA = "yyyy-MM-dd";
    private static final String DATA_PADRAO = "2025-04-21";
    private static final String DATA_PRIMEIRO_PAGAMENTO = "2025-04-21";
    private static final LocalDate DATA_PRIMEIRO_PAGAMENTO_PADRAO = LocalDate.of(2025,4,21);

    @BeforeEach
    void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new AcquisitionConfigurationServiceImpl())
                .build()
                .start();
    }

    private ConfigurationDTO obterMockConfiguracaoSemPlanoEstatutario(String scheduleDate, int parcelas) {
        return ConfigurationDTO.newBuilder()
                .setId(ID_PEDIDO)
                .setCoopId(ID_COOPERATIVA)
                .setSuid(CPF)
                .setAmount(VALOR)
                .setScheduleDate(scheduleDate)
                .setScheduleAccount(AGENDAMENTO_CONTA)
                .setPaymentScheduleConfig(PaymentScheduleConfig.newBuilder()
                        .setInstallments(parcelas)
                        .setFirstPaymentDate("")
                        .setMonthlyAmount(5)
                        .build())
                .build();
    }

    private ConfigurationDTO obterMockConfiguracaoComPlanoEstatutario(String scheduleDate, int parcelas) {
        return ConfigurationDTO.newBuilder()
                .setId(ID_PEDIDO)
                .setCoopId(ID_COOPERATIVA)
                .setSuid(CPF)
                .setAmount(VALOR)
                .setScheduleDate(scheduleDate)
                .setScheduleAccount(AGENDAMENTO_CONTA)
                .setPaymentScheduleConfig(PaymentScheduleConfig.newBuilder()
                        .setInstallments(parcelas)
                        .setFirstPaymentDate(DATA_PRIMEIRO_PAGAMENTO)
                        .setMonthlyAmount(5)
                        .build())
                .build();
    }

    private ConfigurationDTO obterMockConfiguracaoComPlanoCooperativista() {
        return ConfigurationDTO.newBuilder()
                .setId(ID_PEDIDO)
                .setCoopId(ID_COOPERATIVA)
                .setSuid(CPF)
                .setAmount(VALOR)
                .setScheduleDate(DATA_PADRAO)
                .setScheduleAccount(AGENDAMENTO_CONTA)
                .setPaymentScheduleConfig(PaymentScheduleConfig.newBuilder()
                        .setInstallments(99)
                        .setFirstPaymentDate(DATA_PRIMEIRO_PAGAMENTO)
                        .setMonthlyAmount(5)
                        .build())
                .build();
    }

    private void configurarMocksComum(OriginacaoLegado originacaoLegado, String scheduleDate, int parcelas, boolean isEstatutario) {
        var detalheProduto = new DetalheProduto();
        detalheProduto.setIdSimulacao(ID_SIMULACAO);
        originacaoLegado.setCriticas(List.of(new Critica(EtapaProcessoOriginacao.PRODUCT_CAPITAL_LEGACY, MENSAGEM_ERRO)));
        originacaoLegado.setDetalheProduto(Map.of(CAPITAL_LEGACY, detalheProduto));
        ConfigurationDTO expectedResponse = createConfigurationDTO(scheduleDate, parcelas, isEstatutario);
        mockDependencies(expectedResponse, originacaoLegado);
    }

    private ConfigurationDTO createConfigurationDTO(String scheduleDate, int parcelas, boolean isEstatutario) {
        PaymentScheduleConfig.Builder paymentScheduleConfigBuilder = PaymentScheduleConfig.newBuilder()
                .setInstallments(parcelas)
                .setFirstPaymentDate(DATA_PRIMEIRO_PAGAMENTO)
                .setMonthlyAmount(5);

        if (!isEstatutario) {
            paymentScheduleConfigBuilder.setInstallments(0).setFirstPaymentDate("").setMonthlyAmount(0);
        }

        return ConfigurationDTO.newBuilder()
                .setId(ID_PEDIDO)
                .setCoopId(ID_COOPERATIVA)
                .setSuid(CPF)
                .setAmount(VALOR)
                .setScheduleDate(scheduleDate)
                .setScheduleAccount(AGENDAMENTO_CONTA)
                .setPaymentScheduleConfig(paymentScheduleConfigBuilder.build())
                .build();
    }

    private void mockDependencies(ConfigurationDTO expectedResponse, OriginacaoLegado originacaoLegado) {
        doNothing().when(metricasService).incrementCounter(anyString());
        when(proximoDiaUtilService.obterPrimeiroDiaPagamento(anyString(), anyString(), anyString())).thenReturn(LocalDate.now());
        when(numeroContaService.obterNumeroConta(any(OriginacaoLegado.class))).thenReturn("123456");
        when(acquisitionConfigurationServiceBlockingStub.getConfiguration(any(IdentifierDTO.class))).thenReturn(expectedResponse);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);
    }

    @Test
    @DisplayName("Dever buscar dados de configuração de conta capital sem plano estatutário - vencimento em dia útil")
    void deveBuscarDadosDeConfiguracaoDeContaCapitalSemPlanoEstatutarioComVencimentoEmDiaUtil() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        configurarMocksComum(originacaoLegado, DIA_PAGAMENTO, 0, false);
        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create(), originacaoLegado);
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(MASCARA_DATA)), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento().format(DateTimeFormatter.ofPattern(MASCARA_DATA)));
        assertEquals(VALOR, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getValor());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos());
        verify(metricasService, times(1)).incrementCounter(anyString());
        verify(acquisitionConfigurationServiceBlockingStub, times(1)).getConfiguration(any(IdentifierDTO.class));
    }

    @Test
    @DisplayName("Buscar dados de configuração de conta capital sem plano estatutário - vencimento menor que a data atual ou dia NÃO útil")
    void deveBuscarDadosDeConfiguracaoDeContaCapitalSemPlanoEstatutarioComVencimentoMenorQueDataAtualOuDiaNapUtil() {
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        configurarMocksComum(originacaoLegado, DATA_PADRAO, 0, false);
        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create(), originacaoLegado);
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(MASCARA_DATA)), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento().format(DateTimeFormatter.ofPattern(MASCARA_DATA)));
        assertEquals(VALOR, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getValor());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos());
        verify(metricasService, times(1)).incrementCounter(anyString());
        verify(acquisitionConfigurationServiceBlockingStub, times(1)).getConfiguration(any(IdentifierDTO.class));
        verify(proximoDiaUtilService, times(1)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName(" Buscar dados de configuração da conta capital com plano estatutário com 495 parcelas no pedido, deve quebrar em 5 planos de no máximo 99 parcelas por plano.")
    void deveBuscarDadosDeConfiguracaoDeContaCapitalComPlanoEstatutarioCom495ParcelasNoPedido() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        configurarMocksComum(originacaoLegado, DATA_PADRAO, 495, true);
        ConfigurationDTO expectedResponse = obterMockConfiguracaoComPlanoEstatutario(DATA_PADRAO, 495);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(MASCARA_DATA)), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento().format(DateTimeFormatter.ofPattern(MASCARA_DATA)));
        assertEquals(5, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(expectedResponse.getAmount(), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getValor());
        verify(metricasService, times(1)).incrementCounter(anyString());
        verify(acquisitionConfigurationServiceBlockingStub, times(1)).getConfiguration(any(IdentifierDTO.class));
        verify(proximoDiaUtilService, times(1)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName(" Buscar dados de configuração da conta capital com plano estatutário com 120 parcelas no pedido, deve quebrar em 2 planos de no máximo 99 parcelas por plano.")
    void deveBuscarDadosDeConfiguracaoDeContaCapitalComPlanoEstatutarioComMenosDe495Parcelas() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        configurarMocksComum(originacaoLegado, DATA_PADRAO, 120, true);
        ConfigurationDTO expectedResponse = obterMockConfiguracaoComPlanoEstatutario(DATA_PADRAO, 120);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(MASCARA_DATA)), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento().format(DateTimeFormatter.ofPattern(MASCARA_DATA)));
        assertEquals(2, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(99, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().get(0).getParcelas());
        assertEquals(21, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().get(1).getParcelas());
        assertEquals(expectedResponse.getAmount(), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getValor());
        verify(metricasService, times(1)).incrementCounter(anyString());
        verify(acquisitionConfigurationServiceBlockingStub, times(1)).getConfiguration(any(IdentifierDTO.class));
        verify(proximoDiaUtilService, times(1)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());

    }

    @Test
    @DisplayName(" Buscar dados de configuração da conta capital com plano estatutário com menos de 99 parcelas no pedido, deve quebrar em 1 planos com a quantidade de parcelas da simulação.")
    void deveBuscarDadosDeConfiguracaoDeContaCapitalComPlanoEstatutarioComMenosDe99Parcelas() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();

        configurarMocksComum(originacaoLegado, DATA_PADRAO, 60, true);
        ConfigurationDTO expectedResponse = obterMockConfiguracaoComPlanoEstatutario(DATA_PADRAO, 60);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern(MASCARA_DATA)), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento().format(DateTimeFormatter.ofPattern(MASCARA_DATA)));
        assertEquals(1, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(60, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getParcelas());
        assertEquals(expectedResponse.getAmount(), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getValor());
        verify(metricasService, times(1)).incrementCounter(anyString());
        verify(acquisitionConfigurationServiceBlockingStub, times(1)).getConfiguration(any(IdentifierDTO.class));
        verify(proximoDiaUtilService, times(1)).obterPrimeiroDiaPagamento(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve validar se a data da primeira parcela não veio na configuração para um idSimulação")
    void deveValidarQuandoDataPrimeiroVencimentoCapitalForNull() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(null);
        var detalheProduto = new DetalheProduto();
        detalheProduto.setIdSimulacao(ID_SIMULACAO);
        Map<String, DetalheProduto> detalhesProduto = new HashMap<>();
        detalhesProduto.put(CAPITAL_LEGACY, detalheProduto);
        originacaoLegado.setDetalheProduto(detalhesProduto);

        ConfigurationDTO expectedResponse = obterMockConfiguracaoComPlanoEstatutario("", 60);
        when(acquisitionConfigurationServiceBlockingStub.getConfiguration(any(IdentifierDTO.class))).thenReturn(expectedResponse);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);
        when(numeroContaService.obterNumeroConta(any(OriginacaoLegado.class))).thenReturn("321654");
        when(proximoDiaUtilService.obterPrimeiroDiaPagamento(anyString(), anyString(), anyString())).thenThrow(BusinessException.class);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(EtapaProcessoOriginacao.PRODUCT_CAPITAL_LEGACY, originaoLegadoProcessado.getCriticas().getFirst().getCodigo());
    }

    @Test
    @DisplayName("Buscar dados de configuração do plano cooperativista de até 99 parcelas ")
    void deveBuscarDadosDeConfiguracaoDePlanoCooperativistaComAte99Parcelas() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        var detalheProdutoCommercial = new DetalheProduto();
        detalheProdutoCommercial.setIdSimulacao(ID_SIMULACAO);
        var detalheProdutoCapitalLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoCapitalLegacy.setIdSimulacao(ID_SIMULACAO_2);
        detalheProdutoCapitalLegacy.getConfiguracao().getCapital().setDiaPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);

        Map<String, DetalheProduto> detalhesProduto = new HashMap<>();
        detalhesProduto.put(CAPITAL_COMMERCIAL_PLAN_LEGACY, detalheProdutoCommercial);
        detalhesProduto.put(CAPITAL_LEGACY, detalheProdutoCapitalLegacy);
        originacaoLegado.setDetalheProduto(detalhesProduto);

        ConfigurationDTO expectedResponse = obterMockConfiguracaoComPlanoCooperativista();
        doNothing().when(metricasService).incrementCounter(anyString());
        when(acquisitionConfigurationServiceBlockingStub.getConfiguration(any(IdentifierDTO.class))).thenReturn(expectedResponse);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getDiaPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getValor());
        assertEquals(1, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(99, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getParcelas());
    }

    @Test
    @DisplayName("Deve modificar data de primeiro pagamento de todos os planos e no capital inicial.")
    void deveModificarDataDePrimeiroVencimentoDiaPagamentoTodosPlanos() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(List.of(new Critica(PRODUCT_CAPITAL_LEGACY, "Teste", "Teste")));

        var expectedResponse = ConfigurationDTO.newBuilder()
                .setId(ID_PEDIDO)
                .setCoopId(ID_COOPERATIVA)
                .setSuid(CPF)
                .setAmount(VALOR)
                .setScheduleDate(DATA_PADRAO)
                .setScheduleAccount(AGENDAMENTO_CONTA)
                .setPaymentScheduleConfig(PaymentScheduleConfig.newBuilder()
                        .setInstallments(99)
                        .setFirstPaymentDate(DATA_PRIMEIRO_PAGAMENTO)
                        .setMonthlyAmount(5)
                        .build())
                .build();

        var detalheProdutoCapitalLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoCapitalLegacy.setIdSimulacao(ID_SIMULACAO);
        detalheProdutoCapitalLegacy.getConfiguracao().getCapital().setDiaPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);

        var detalheProdutoCommercialPlanLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoCommercialPlanLegacy.setIdSimulacao(ID_SIMULACAO);
        detalheProdutoCommercialPlanLegacy.getConfiguracao().getCapital().setDiaPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);
        var primeiroPlanoEstatutario = Instancio.of(Plano.class).create();
        primeiroPlanoEstatutario.setDiaPrimeiroPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);
        detalheProdutoCommercialPlanLegacy.getConfiguracao().getCapital().getPlanos().set(0, primeiroPlanoEstatutario);

        Map<String, DetalheProduto> detalheProdutoMap = new HashMap<>();
        detalheProdutoMap.put(CAPITAL_COMMERCIAL_PLAN_LEGACY, detalheProdutoCapitalLegacy);
        detalheProdutoMap.put(CAPITAL_LEGACY, detalheProdutoCommercialPlanLegacy);
        originacaoLegado.setDetalheProduto(detalheProdutoMap);

        doNothing().when(metricasService).incrementCounter(anyString());
        when(acquisitionConfigurationServiceBlockingStub.getConfiguration(any(IdentifierDTO.class))).thenReturn(expectedResponse);
        when(proximoDiaUtilService.obterPrimeiroDiaPagamento(anyString(), anyString(), anyString())).thenReturn(DATA_PRIMEIRO_PAGAMENTO_PADRAO);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(DATA_PRIMEIRO_PAGAMENTO_PADRAO, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento());
        assertEquals(LocalDate.of(2025,4,22), originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getDiaPrimeiroPagamento());
        assertEquals(LocalDate.of(2025,4,23),originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getDiaPrimeiroPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getDiaPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getValor());
        assertEquals(1, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(99, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getParcelas());
    }

    @Test
    @DisplayName("Deve modificar data de primeiro pagamento do plano comercial quando não tem o plano estatutario")
    void deveModificarDataDePrimeiroVencimentoPlanoComercialQuandoTemSomenteCapitalIncialEPlanoComercial() {
        var acquisitionEngineManagerItemsEventDTO = Instancio.of(AcquisitionEngineManagerItemsEventDTO.class).create();
        var originacaoLegado = Instancio.of(OriginacaoLegado.class).create();
        originacaoLegado.setCriticas(List.of(new Critica(PRODUCT_CAPITAL_LEGACY, "Teste", "Teste")));

        var detalheProdutoCapitalLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoCapitalLegacy.setIdSimulacao(ID_SIMULACAO);
        detalheProdutoCapitalLegacy.getConfiguracao().getCapital().setDiaPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);

        var detalheProdutoCommercialPlanLegacy = Instancio.of(DetalheProduto.class).create();
        detalheProdutoCommercialPlanLegacy.setIdSimulacao(ID_SIMULACAO_2);
        detalheProdutoCommercialPlanLegacy.getConfiguracao().getCapital().setDiaPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);

        var primeiroPlanoEstatutario = Instancio.of(Plano.class).create();
        primeiroPlanoEstatutario.setDiaPrimeiroPagamento(DATA_PRIMEIRO_PAGAMENTO_PADRAO);
        detalheProdutoCommercialPlanLegacy.getConfiguracao().getCapital().getPlanos().set(0, primeiroPlanoEstatutario);

        Map<String, DetalheProduto> detalheProdutoMap = new HashMap<>();
        detalheProdutoMap.put(CAPITAL_COMMERCIAL_PLAN_LEGACY, detalheProdutoCapitalLegacy);
        detalheProdutoMap.put(CAPITAL_LEGACY, detalheProdutoCommercialPlanLegacy);
        originacaoLegado.setDetalheProduto(detalheProdutoMap);

        doNothing().when(metricasService).incrementCounter(anyString());
        when(acquisitionConfigurationServiceBlockingStub.getConfiguration(any()))
                .thenAnswer(invocation -> {
                    IdentifierDTO request = invocation.getArgument(0);

                    if (ID_SIMULACAO.equals(request.getId())) {
                        return obterMockConfiguracaoComPlanoEstatutario(DATA_PADRAO, 99);
                    } else if (ID_SIMULACAO_2.equals(request.getId())) {
                        return obterMockConfiguracaoSemPlanoEstatutario(DATA_PADRAO, 99);
                    } else {
                        return null;
                    }
                });

        when(proximoDiaUtilService.obterPrimeiroDiaPagamento(anyString(), anyString(), anyString())).thenReturn(DATA_PRIMEIRO_PAGAMENTO_PADRAO);
        when(originacaoLegadoMapper.merge(any(OriginacaoLegado.class))).thenReturn(originacaoLegado);

        var originaoLegadoProcessado = capitalAccountAcquisitionHandler.processarProximo(acquisitionEngineManagerItemsEventDTO, originacaoLegado);

        assertEquals(DATA_PRIMEIRO_PAGAMENTO_PADRAO, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getDiaPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_LEGACY).getConfiguracao().getCapital().getPlanos());
        assertEquals(LocalDate.of(2025,4,22),originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getDiaPrimeiroPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getDiaPagamento());
        assertNull(originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getValor());
        assertEquals(1, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().size());
        assertEquals(99, originaoLegadoProcessado.getDetalheProduto().get(CAPITAL_COMMERCIAL_PLAN_LEGACY).getConfiguracao().getCapital().getPlanos().getFirst().getParcelas());
    }


    private static class AcquisitionConfigurationServiceImpl extends AcquisitionConfigurationServiceGrpc.AcquisitionConfigurationServiceImplBase {
        @Override
        public void getConfiguration(IdentifierDTO request, StreamObserver<ConfigurationDTO> responseObserver) {
            ConfigurationDTO response = ConfigurationDTO.newBuilder()
                    .setId(request.getId())
                    .setCoopId(ID_COOPERATIVA)
                    .setSuid(CPF)
                    .setAmount(VALOR)
                    .setScheduleDate(DIA_PAGAMENTO)
                    .setScheduleAccount(AGENDAMENTO_CONTA)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}