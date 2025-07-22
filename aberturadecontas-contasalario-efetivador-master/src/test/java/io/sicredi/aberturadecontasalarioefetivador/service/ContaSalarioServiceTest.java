package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalario;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalarioservice.ContaSalarioServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.entities.*;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.CriarContaSalarioResponseFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaSalarioServiceTest {

    public static final String COD_STATUS_SUCESSO = "000";
    public static final String DESC_STATUS_SUCESSO = " - A CONTA SALARIO FOI CRIADA COM SUCESSO!";
    public static final String NUMERO_CONTA_1 = "892824";
    public static final String TRANSACTION_ID = "1234567890";
    public static final String RESPONSE_TOPIC = "sucessoCadastroTopic";
    public static final String RESPONSE_ERROR_TOPIC = "erroCadastroTopic";
    public static final String NUM_CONTA_DESTINO_DIGITAL = "001234";
    @Mock
    private ContaSalarioServiceClient client;
    @Mock
    private CadastroRepository cadastroRepository;
    @Mock
    private CadastroAssociadoContasService cadastroAssociadoContasService;
    @Mock
    private MetricService metricService;
    @InjectMocks
    private ContaSalarioService service;

    @Test
    @DisplayName("Deve criar Conta Salário modalidade saque com sucesso")
    void deveCriarContaSalarioModalidadeSaqueComSucesso() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, 0);

        when(client.criarContaSalario(any(CriarContaSalario.class)))
                .thenReturn(criarContaSalarioResponse);

        var retornado = service.criarContaSalario(cadastro, false);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodConvenioFontePagadora()).isEqualTo(solicitacao.getCodConvenioFontePagadora());
        assertThat(retornado.getContaSalarioResponse().getNumCPF()).isEqualTo(cadastro.getCpf());
        assertThat(retornado.getContaSalarioResponse().getNumCooperativa()).isEqualTo(solicitacao.getNumCooperativa());

        assertThat(retornado.getContaSalarioResponse().getNumAgencia()).isEqualTo(solicitacao.getNumAgencia());
        assertThat(retornado.getContaSalarioResponse().getNumConta()).isEqualTo(NUMERO_CONTA_1);
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isEqualTo(COD_STATUS_SUCESSO);
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isEqualTo(DESC_STATUS_SUCESSO);

        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
    }

    @Test
    @DisplayName("Não deve criar Conta Salário modalidade saque")
    void naoDeveCriarContaSalarioModalidadeSaqueQuandoErroERetornarContaNullComCritica() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseErroGenerico(solicitacao, 0);

        when(client.criarContaSalario(any(CriarContaSalario.class)))
                .thenReturn(criarContaSalarioResponse);

        var retornado = service.criarContaSalario(cadastro, false);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodConvenioFontePagadora()).isEqualTo(solicitacao.getCodConvenioFontePagadora());
        assertThat(retornado.getContaSalarioResponse().getNumCPF()).isEqualTo(cadastro.getCpf());
        assertThat(retornado.getContaSalarioResponse().getNumCooperativa()).isEqualTo(solicitacao.getNumCooperativa());

        assertThat(retornado.getContaSalarioResponse().getNumAgencia()).isEqualTo(solicitacao.getNumAgencia());
        assertThat(retornado.getContaSalarioResponse().getNumConta()).isNull();
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isNotEqualTo(COD_STATUS_SUCESSO);
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isNotEqualTo(DESC_STATUS_SUCESSO);
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isNotNull();

        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
    }

    @Test
    @DisplayName("Deve criar Conta Salário modalidade Portabilidade Conta Pessoa Digital com sucesso")
    void deveCriarContaSalarioModalidadePortabilidadePessoaDigitalComSucesso() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, 0);
        criarContaSalarioResponse.getContaSalarioResponse().setNumConta("1234");

        cadastro.setPortabilidade(Portabilidade.builder()
                .codBancoDestino("748")
                .numAgDestino("0119")
                .numContaDestino(NUM_CONTA_DESTINO_DIGITAL)
                .tipoConta(TipoConta.CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF)
                .build());

        when(client.criarContaSalario(any(CriarContaSalario.class)))
                .thenReturn(criarContaSalarioResponse);

        var retornado = service.criarContaSalario(cadastro, true);

        assertThat(retornado.getContaSalarioResponse()).isNotNull();
        assertThat(retornado.getContaSalarioResponse().getCodConvenioFontePagadora()).isEqualTo(solicitacao.getCodConvenioFontePagadora());
        assertThat(retornado.getContaSalarioResponse().getNumCPF()).isEqualTo(cadastro.getCpf());
        assertThat(retornado.getContaSalarioResponse().getNumCooperativa()).isEqualTo(solicitacao.getNumCooperativa());

        assertThat(retornado.getContaSalarioResponse().getNumAgencia()).isEqualTo(solicitacao.getNumAgencia());
        assertThat(retornado.getContaSalarioResponse().getNumConta()).isEqualTo(NUM_CONTA_DESTINO_DIGITAL.substring(2));
        assertThat(retornado.getContaSalarioResponse().getCodStatus()).isEqualTo(COD_STATUS_SUCESSO);
        assertThat(retornado.getContaSalarioResponse().getDesStatus()).isEqualTo(DESC_STATUS_SUCESSO);

        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
    }

    @Test
    @DisplayName("Deve processar solicitação unitária de Cadastro de Conta Salário com sucesso")
    void deveProcessarSolicitacaoUnitariaDeCadastroDeContaSalario() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, 0);
        
        deveProvessarSolicitacaoUnitariaDeCadastroSemCriticaQuandoSucesso(solicitacao, criarContaSalarioResponse, false);
    }

    @Test
    @DisplayName("Deve processar solicitação unitária de Cadastro de Conta Salário com sucesso e sem crítica")
    void deveProcessarSolicitacaoUnitariadeCadastroDeContaSalarioSemCritica() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucessoComCritica(solicitacao, 0);

        deveProvessarSolicitacaoUnitariaDeCadastroSemCriticaQuandoSucesso(solicitacao, criarContaSalarioResponse, false);
    }

    @Test
    @DisplayName("Deve processar solicitação unitária de Cadastro de Conta Salário Pessoa Digital com sucesso e com crítica Informativo")
    void deveProcessarSolicitacaoUnitariadeCadastroDeContaSalarioComCriticaPessoaDigital() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseSucessoComCritica(solicitacao, 0);
        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString());

        deveProvessarSolicitacaoUnitariaDeCadastroSemCriticaQuandoSucesso(solicitacao, criarContaSalarioResponse, true);
        verify(metricService, times(1)).incrementCounter(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve processar solicitação unitária de Cadastro de Conta Salário com erro e com crítica")
    void deveProcessarSolicitacaoUnitariadeCadastroDeContaSalarioComErroCritica() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var cadastro = solicitacao.getCadastros().getFirst();
        var criarContaSalarioResponse =
                CriarContaSalarioResponseFactory.criarContaSalarioResponseErroGenerico(solicitacao, 0);
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(cadastroAssociadoContasService.isAssociadoDigital(cadastro.getCpf())).thenReturn(false);
        when(client.criarContaSalario(any(CriarContaSalario.class))).thenReturn(criarContaSalarioResponse);
        doNothing().when(metricService).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());

        var cadastroIdempotentResponse = service.processarSolicitacaoUnitaria(idempotentAsyncRequest);

        assertThat(cadastroIdempotentResponse.getValue().getConta()).isEqualTo(criarContaSalarioResponse.getContaSalarioResponse().getNumConta());
        assertThat(cadastroIdempotentResponse.getValue().isProcessado()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().isEfetivado()).isFalse();
        assertThat(cadastroIdempotentResponse.isErrorResponse()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().getCriticas()).isNotEmpty();
        assertThat(cadastroIdempotentResponse.getValue().getSituacao()).isEqualTo(Resultado.ERRO);

        verify(cadastroRepository, times(1)).findById(cadastro.getId());
        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
        verify(metricService, times(1)).incrementCounter(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando solicitacão de Cadastro unitária não existir no banco de dados")
    void deveRetornarNotFoundExceptionQuandoNaoEncontrarCadastroAtravesDoId() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var cadastro = solicitacao.getCadastros().getFirst();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();
        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.processarSolicitacaoUnitaria(idempotentAsyncRequest));

        verify(cadastroRepository, times(1)).findById(cadastro.getId());
        verify(client, times(0)).criarContaSalario(any(CriarContaSalario.class));
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
    }

    @Test
    @DisplayName("Deve lançar WebserviceException quando contaSalarioServiceClient lançar exceção ")
    void deveLancarWebserviceException() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var cadastro = solicitacao.getCadastros().getFirst();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(cadastroAssociadoContasService.isAssociadoDigital(cadastro.getCpf())).thenReturn(false);
        when(client.criarContaSalario(any(CriarContaSalario.class))).thenThrow(WebserviceException.class);

        assertThrows(WebserviceException.class,
                () -> service.processarSolicitacaoUnitaria(idempotentAsyncRequest));

        verify(cadastroRepository, times(1)).findById(cadastro.getId());
        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
    }

    @Test
    @DisplayName("Deve processar resposta de solicitação de Cadastro unitário com sucesso")
    void deveProcessarRespoostaDeSolicitacaoDeCadastroUnitarioComSucesso() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        Cadastro cadastro = solicitacao.getCadastros().getFirst();
        cadastro.setProcessado(true);
        cadastro.setEfetivado(true);
        cadastro.setSituacao(Resultado.CONCLUIDO);
        cadastro.setConta("1234567");
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));

        var cadastroIdempotentResponse = service.processarRespostaSolicitacaoUnitaria(idempotentAsyncRequest);

        assertThat(cadastroIdempotentResponse.getValue().getConta()).isEqualTo(cadastro.getConta());
        assertThat(cadastroIdempotentResponse.getValue().isProcessado()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().isEfetivado()).isTrue();
        assertThat(cadastroIdempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroIdempotentResponse.getValue().getCriticas()).isEmpty();
        assertThat(cadastroIdempotentResponse.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);

        verify(cadastroRepository, times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("Deve processar resposta de solicitação de Cadastro unitário com sucesso e finalizar solicitação")
    void deveProcessarRespoostaDeSolicitacaoDeCadastroUnitarioComSucessoEFinalizarSolicitacao() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos();

        processaRespostaDeSolicitacaoDeCadastroUnitario(solicitacao);
    }

    @Test
    @DisplayName("Deve processar resposta de solicitação de Cadastro unitário com sucesso e finalizar solicitação parcialmente com crítica")
    void deveProcessarRespoostaDeSolicitacaoDeCadastroUnitarioComSucessoEFinalizarSolicitacaoParcialmente() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidosParcialmente();

        processaRespostaDeSolicitacaoDeCadastroUnitario(solicitacao);
    }

    @Test
    @DisplayName("Deve processar resposta de solicitação de Cadastro unitário com sucesso e finalizar solicitação com erro")
    void deveProcessarRespostaDeSolicitacaoDeCadastroUnitarioComSucessoEFinalizarSolicitacaoComErro() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosComErro();

        processaRespostaDeSolicitacaoDeCadastroUnitario(solicitacao);
    }

    private void processaRespostaDeSolicitacaoDeCadastroUnitario(Solicitacao solicitacao) {
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();

        cadastro.setSolicitacao(solicitacao);
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));

        var cadastroIdempotentResponse = service.processarRespostaSolicitacaoUnitaria(idempotentAsyncRequest);

        assertThat(cadastroIdempotentResponse.getValue().getConta()).isEqualTo(cadastro.getConta());
        assertThat(cadastroIdempotentResponse.getValue().isProcessado()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().isEfetivado()).isTrue();
        assertThat(cadastroIdempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroIdempotentResponse.getValue().getCriticas()).isEmpty();
        assertThat(cadastroIdempotentResponse.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);

        verify(cadastroRepository, times(1)).findById(cadastro.getId());
    }

    private void deveProvessarSolicitacaoUnitariaDeCadastroSemCriticaQuandoSucesso(Solicitacao solicitacao,
                                                                                   CriarContaSalarioResponse criarContaSalarioResponse,
                                                                                   boolean isRelacionamentoDigital) {
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var cadastro = solicitacao.getCadastros().getFirst();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(cadastroAssociadoContasService.isAssociadoDigital(cadastro.getCpf())).thenReturn(isRelacionamentoDigital);
        when(client.criarContaSalario(any(CriarContaSalario.class))).thenReturn(criarContaSalarioResponse);

        var cadastroIdempotentResponse = service.processarSolicitacaoUnitaria(idempotentAsyncRequest);

        assertThat(cadastroIdempotentResponse.getValue().getConta())
                .isEqualTo(criarContaSalarioResponse.getContaSalarioResponse().getNumConta());
        assertThat(cadastroIdempotentResponse.getValue().isProcessado()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().isEfetivado()).isTrue();
        assertThat(cadastroIdempotentResponse.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);

        if (isRelacionamentoDigital) {
            assertThat(cadastroIdempotentResponse.getValue().getCriticas()).isNotEmpty();
            assertThat(cadastroIdempotentResponse.getValue().getCriticas().size()).isOne();
            cadastroIdempotentResponse.getValue().getCriticas().forEach(critica -> {
                assertThat(critica.getCodigo()).isEqualTo("CCS018");
                assertThat(critica.getDescricao()).isEqualTo("Associado Digital - Dados cadastrais não atualizados.");
            });
        }
        else {
            assertThat(cadastroIdempotentResponse.getValue().getCriticas()).isEmpty();
        }

        verify(cadastroRepository, times(1)).findById(cadastro.getId());
        verify(client, times(1)).criarContaSalario(any(CriarContaSalario.class));
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
    }
}