package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalarioResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociadoResponse;
import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConsultarContaSalarioServiceTest {

    private static final String DOCUMENTO = "36185900084";
    private static final String NUMERO_CONTA_SALARIO = "903677";
    private static final String NUMERO_CONTA_SALARIO_ENCERRADA = "903676";
    private static final String AGENCIA_CONTA_SALARIO = "0167";
    private static final String CODIGO_CONVENIO = "3AO";
    private static final String SIM = "SIM";
    private static final String NA = "NAO";
    public static final String CANAL = "MEU_CANAL";

    @Mock
    private CadastroAssociadoService cadastroAssociadoService;
    @Mock
    private CadastroAssociadoContasService cadastroAssociadoContasService;
    @Mock
    private AberturaContaCoexistenciaService aberturaContaCoexistenciaService;
    @Mock
    private ContaSalarioContasService contaSalarioContasService;
    @Mock
    private ContaSalarioCoreEventosService contaSalarioCoreEventosService;
    @Mock
    private MetricService metricService;

    @InjectMocks
    private ConsultarContaSalarioService consultarContaSalarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve consultar dados pessoa e retornar vazio quando nao encontrar documento")
    void deveConsultarContaSalarioEQuandoPessoaNaoEncontradaRetornaListaVazia() {
        ConsultarDadosAssociadoResponse dadosAssociadoVazio = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseSemElementos();

        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());
        when(cadastroAssociadoService.consultarDadosAssociado(anyString())).thenReturn(dadosAssociadoVazio);

        List<ConsultarContaSalarioResponseDTO> retornado =
                consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);

        assertThat(retornado).isEmpty();
        verify(cadastroAssociadoService).consultarDadosAssociado(DOCUMENTO);
    }

    @Test
    @DisplayName("Deve consultar dados pessoa e retornar vazio quando nao encontrar conta salário")
    void deveConsultarContaSalarioEQuandoPessoaSemContaSalarioRetornaListaVazia() {
        ConsultarDadosAssociadoResponse resp = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseValido();

        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());
        when(cadastroAssociadoService.consultarDadosAssociado(anyString())).thenReturn(resp);
        when(cadastroAssociadoContasService.buscarContasSalarioAssociado(anyString()))
                .thenReturn(Collections.emptyList());

        List<ConsultarContaSalarioResponseDTO> retornado =
                consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);

        assertThat(retornado).isEmpty();
    }

    @Test
    @DisplayName("Deve consultar documento com conta salário e retornar detalhes da conta")
    void deveConsultarDocumentoComContaSalarioERetornarDetalhesDaConta() {
        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());
        when(cadastroAssociadoService.consultarDadosAssociado(DOCUMENTO))
                .thenReturn(ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseValido());

        List<CadastroAssociadoContasDTO> contas = List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false));
        when(cadastroAssociadoContasService.buscarContasSalarioAssociado(DOCUMENTO))
                .thenReturn(contas);
        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString()))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.getContaSalarioResponse());
        when(contaSalarioContasService.consultarContaSalario(anyString(), anyString()))
                .thenReturn(ConsultarSaldoContaSalarioFactory.consultarSaldoContaSalarioResponse());
        when(contaSalarioCoreEventosService.buscarEventosAlteracaoContaSalario(anyString(), anyString()))
                .thenReturn(ContaSalarioCoreEventosFactory.contaSalarioEventosAlteracao());

        List<ConsultarContaSalarioResponseDTO> retornado =
                consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);

        assertThat(retornado).hasSize(1);
        ConsultarContaSalarioResponseDTO dto = retornado.getFirst();
        assertThat(dto.nome()).isEqualTo("VALOR DEFAULT MOCK");
        assertThat(dto.documento()).isEqualTo(DOCUMENTO);
        assertThat(dto.saldoAtual()).isEqualTo(SIM);
        assertThat(dto.saldoAnterior()).isEqualTo(NA);
        assertThat(dto.convenio().codigo()).isEqualTo(CODIGO_CONVENIO);
        assertThat(dto.portabilidade().banco()).isEqualTo("336");
        assertThat(dto.alteracoes()).isNotNull();
        assertThat(dto.alteracoes()).hasSize(5);

        verify(cadastroAssociadoService).consultarDadosAssociado(DOCUMENTO);
        verify(cadastroAssociadoContasService).buscarContasSalarioAssociado(DOCUMENTO);
        verify(aberturaContaCoexistenciaService).consultarContaSalario(NUMERO_CONTA_SALARIO, AGENCIA_CONTA_SALARIO);
        verify(contaSalarioContasService).consultarContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO);
        verify(contaSalarioCoreEventosService).buscarEventosAlteracaoContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO);
    }

    @Test
    @DisplayName("Deve consultar documento com conta salário e retornar detalhes da conta com Encerrada")
    void deveConsultarDocumentoComContaSalarioERetornarDetalhesDaContaComEncerrada() {
        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());
        when(cadastroAssociadoService.consultarDadosAssociado(DOCUMENTO))
                .thenReturn(ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseValido());

        List<CadastroAssociadoContasDTO> contas =
                List.of(
                        CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false),
                        CadastroAssociadoContasFactory.criaCadastroAssociadoContaEncerrada()
                );

        when(cadastroAssociadoContasService.buscarContasSalarioAssociado(DOCUMENTO))
                .thenReturn(contas);

        when(aberturaContaCoexistenciaService.consultarContaSalario(NUMERO_CONTA_SALARIO, AGENCIA_CONTA_SALARIO))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.getContaSalarioResponse());
        when(aberturaContaCoexistenciaService.consultarContaSalario(NUMERO_CONTA_SALARIO_ENCERRADA, AGENCIA_CONTA_SALARIO))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.getContaSalarioEncerradaResponse());

        when(contaSalarioContasService.consultarContaSalario(anyString(), anyString()))
                .thenReturn(ConsultarSaldoContaSalarioFactory.consultarSaldoContaSalarioResponse());

        when(contaSalarioCoreEventosService.buscarEventosAlteracaoContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO))
                .thenReturn(ContaSalarioCoreEventosFactory.contaSalarioEventosAlteracao());

        List<ConsultarContaSalarioResponseDTO> retornado =
                consultarContaSalarioService.consultarContaSalario(DOCUMENTO, CODIGO_CONVENIO, CANAL);

        assertThat(retornado).hasSize(2);
        ConsultarContaSalarioResponseDTO contaAtiva = retornado.getFirst();
        assertThat(contaAtiva.nome()).isEqualTo("VALOR DEFAULT MOCK");
        assertThat(contaAtiva.conta()).isEqualTo(NUMERO_CONTA_SALARIO);
        assertThat(contaAtiva.documento()).isEqualTo(DOCUMENTO);
        assertNull(contaAtiva.dataEncerramento());
        assertThat(contaAtiva.status()).isEqualTo("ATIVA");
        assertThat(contaAtiva.saldoAtual()).isEqualTo(SIM);
        assertThat(contaAtiva.saldoAnterior()).isEqualTo(NA);
        assertThat(contaAtiva.convenio().codigo()).isEqualTo(CODIGO_CONVENIO);
        assertThat(contaAtiva.portabilidade().banco()).isEqualTo("336");
        assertThat(contaAtiva.alteracoes()).isNotNull();
        assertThat(contaAtiva.alteracoes()).hasSize(5);

        ConsultarContaSalarioResponseDTO contaEncerrada = retornado.getLast();
        assertThat(contaEncerrada.conta()).isEqualTo(NUMERO_CONTA_SALARIO_ENCERRADA);
        assertNotNull(contaEncerrada.dataEncerramento());
        assertThat(contaEncerrada.dataEncerramento()).isEqualTo("2024-10-07");
        assertThat(contaEncerrada.status()).isEqualTo("ENCERRADA");
        assertThat(contaEncerrada.alteracoes()).isNull();

        verify(cadastroAssociadoService).consultarDadosAssociado(DOCUMENTO);
        verify(cadastroAssociadoContasService).buscarContasSalarioAssociado(DOCUMENTO);
        verify(aberturaContaCoexistenciaService).consultarContaSalario(NUMERO_CONTA_SALARIO, AGENCIA_CONTA_SALARIO);
        verify(aberturaContaCoexistenciaService).consultarContaSalario(NUMERO_CONTA_SALARIO_ENCERRADA, AGENCIA_CONTA_SALARIO);
        verify(contaSalarioContasService).consultarContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO);
        verify(contaSalarioCoreEventosService).buscarEventosAlteracaoContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO);
        verify(contaSalarioCoreEventosService, times(0)).buscarEventosAlteracaoContaSalario(AGENCIA_CONTA_SALARIO, NUMERO_CONTA_SALARIO_ENCERRADA);

    }

    @Test
    @DisplayName("Deve consultar documento com conta salário e retornar vazio quando conta nao for do convenio ")
    void deveConsultarContaSalarioERetornarVazioQuandoContaDiferenteDoConvenio() {
        when(cadastroAssociadoService.consultarDadosAssociado(anyString()))
                .thenReturn(ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponseValido());
        when(cadastroAssociadoContasService.buscarContasSalarioAssociado(anyString()))
                .thenReturn(List.of(CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false)));

        GetContaSalarioResponse contaSalario = AberturaContaCoexistenciaServiceFactory.getContaSalarioResponse();
        contaSalario.getReturn().setCodEmpresaConvenio(CODIGO_CONVENIO);
        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString()))
                .thenReturn(contaSalario);

        List<ConsultarContaSalarioResponseDTO> retornado =
                consultarContaSalarioService.consultarContaSalario(DOCUMENTO, "999", "");

        assertThat(retornado).isEmpty();
        verifyNoInteractions(metricService);
    }
}