package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.*;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.AgenciaECooperativaNaoCorrespondentesException;
import io.sicredi.aberturadecontasalarioefetivador.factories.CriarContaSalarioResponseFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoRequestDTOFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoResponseDTOFactory;
import io.sicredi.aberturadecontasalarioefetivador.mapper.SolicitacaoMapper;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;
    @Mock
    private CadastroRepository cadastroRepository;
    @Mock
    private WebhookService webhookService;
    @Mock
    private SolicitacaoMapper mapper;
    @Mock
    private HeaderService headerService;
    @Mock
    private GestentService gestentService;
    @Mock
    private MetricService metricService;
    @Mock
    private ContaSalarioService contasalarioService;
    @InjectMocks
    private SolicitacaoService service;
    private static final String TRANSACTION_ID = "1234567890";
    private static final String RESPONSE_TOPIC = "sucessoCadastroTopic";
    private static final String RESPONSE_ERROR_TOPIC = "erroCadastroTopic";
    private static final int WEBHOOK_CODIGO_SUCESSO = 202;
    private static final BigInteger TRANSACTIONID = new BigInteger("202409301181156156165196151");
    private static final String CABECALHO_EXTERNO_CANAL = "Canal";
    private static final String CABECALHO_INTERNO_TRANSACTION_ID_SOLICITACAO = "transactionIdSolicitacao";
    private static final String CANAL_EXTERNO = "EXTERNO";

    @Test
    @DisplayName("Deve retornar solicitação por idTransacao")
    void deveRetornarSolicitacaoPorIdTransacaoQuandoEncontrar() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var expectedResponse = SolicitacaoResponseDTOFactory.solicitacaoResponseDTODoisCadastros();

        when(mapper.map(solicitacao)).thenReturn(expectedResponse);
        when(solicitacaoRepository.findByIdTransacao(TRANSACTIONID)).thenReturn(Optional.of(solicitacao));

        var retornado = service.consultarSolicitacao(TRANSACTIONID);

        assertThat(retornado).isEqualTo(expectedResponse);

        verify(mapper, times(1)).map(solicitacao);
        verify(solicitacaoRepository, times(1)).findByIdTransacao(TRANSACTIONID);
    }

    @Test
    @DisplayName("Não deve retornar solicitação por idTransacao quando não encontrar")
    void naoDeveRetornarSolicitacaoPorIdTransacaoQuandoNaoEncontrar() {
        when(solicitacaoRepository.findByIdTransacao(TRANSACTIONID)).thenReturn(Optional.empty());

        var retornado = service.consultarSolicitacao(TRANSACTIONID);

        assertThat(retornado).isNotNull();
        assertThat(retornado.cadastros()).isNull();
        assertThat(retornado.idTransacao()).isBlank();

        verify(solicitacaoRepository, times(1)).findByIdTransacao(TRANSACTIONID);
    }

    @Test
    @DisplayName("Deve retornar solicitação completa por idTransacao")
    void deveRetornarSolicitacaoCompletaPorIdTransacaoQuandoEncontrar() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();

        when(solicitacaoRepository.findByIdTransacao(TRANSACTIONID)).thenReturn(Optional.of(solicitacao));

        var retornado = service.consultarSolicitacaoCompleta(TRANSACTIONID);

        assertThat(retornado).isPresent().contains(solicitacao);

        verifyNoInteractions(mapper);
        verify(solicitacaoRepository, times(1)).findByIdTransacao(TRANSACTIONID);
    }

    @Test
    @DisplayName("Deve inicializar e persistir a solicitação e seus cadastros")
    void deveInicializarEPersistirSolicitacaoESeusCadastros() {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var expectedResponseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTODoisCadastros();
        var idempotentRequest = mapIdempotenteRequest(requestDTO);

        prepararMocksParaProcessamentoDeSolicitacao(expectedResponseDTO, solicitacao);

        var idempotentResponse = service.processarSolicitacao(idempotentRequest, idempotentRequest.getTransactionId(), CANAL_EXTERNO, "XPTO");
        var responseDTO = idempotentResponse.getValue();

        assertSolicitacaoProcessada(idempotentResponse, expectedResponseDTO, responseDTO);
    }

    @Test
    @DisplayName("Deve reprocessar solicitação (inicializar e persistir a solicitação já existente e seus cadastros)")
    void deveReprocessarSolicitacao() {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var expectedResponseDTO = SolicitacaoResponseDTOFactory.solicitacaoResponseDTODoisCadastros();
        var idempotentRequest = IdempotentRequest.<String>builder()
                .transactionId(TRANSACTION_ID + "123")
                .value(solicitacao.getIdTransacao().toString())
                .build();
        when(mapper.mapToRequest(any(Solicitacao.class))).thenReturn(requestDTO);
        when(solicitacaoRepository.findByIdTransacao(any(BigInteger.class)))
                .thenReturn(Optional.of(solicitacao));

        prepararMocksParaProcessamentoDeSolicitacao(expectedResponseDTO, solicitacao);

        var idempotentResponse = service.reprocessarSolicitacao(idempotentRequest);
        var responseDTO = idempotentResponse.getValue();

        assertSolicitacaoProcessada(idempotentResponse, expectedResponseDTO, responseDTO);
        verify(mapper, times(1)).mapToRequest(any(Solicitacao.class));
        verify(solicitacaoRepository, times(1)).findByIdTransacao(any(BigInteger.class));
    }

    @Test
    @DisplayName("Deve inicializar e persistir a solicitação e seus cadastros, e iniciar o processamento dos cadastros")
    void deveIniciarPersistirEProcessarASolicitacaoEOsCadastrosDeContaCorrente() {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var criarContaSalarioResponse = CriarContaSalarioResponseFactory.criarContaSalarioResponseSucesso(solicitacao, 1);

        when(contasalarioService.criarContaSalario(any(Cadastro.class), anyBoolean())).thenReturn(criarContaSalarioResponse);
        when(gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(solicitacao.getNumCooperativa(), solicitacao.getNumAgencia())).thenReturn("ACA");
        when(mapper.map(any(SolicitacaoRequestDTO.class))).thenReturn(solicitacao);
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> {
            Solicitacao argument = invocation.getArgument(0);
            List<Cadastro> cadastros = argument.getCadastros();
            IntStream.range(0, cadastros.size()).forEach(i -> cadastros.get((i)).setId((long) i + 1));
            return argument.toBuilder().id(1L).build();
        });

        var idempotentResponse = service.processarSolicitacaoClient(requestDTO, TRANSACTION_ID, CANAL_EXTERNO, "XPTO");

        assertThat(idempotentResponse).isNotNull().hasSize(2)
                .allMatch(response -> response.getContaSalarioResponse().getNumConta()
                        .equals(criarContaSalarioResponse.getContaSalarioResponse().getNumConta()));

        verify(contasalarioService, times(2)).criarContaSalario(any(Cadastro.class), anyBoolean());
        verify(gestentService, times(1))
                .consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString());
        verify(mapper, times(1)).map(any(SolicitacaoRequestDTO.class));
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando falhar ao persistir nova solicitação e cadastros")
    void deveLancarExcessaoQuandoFalharAoPersistirSolicitacaoESeusCadastros() {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var idempotentRequest = mapIdempotenteRequest(requestDTO);
        var transactionId = idempotentRequest.getTransactionId();

        when(gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString()))
                .thenReturn("ACA");
        when(mapper.map(any(SolicitacaoRequestDTO.class))).thenReturn(solicitacao);
        when(solicitacaoRepository.save(any(Solicitacao.class)))
                .thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> service.processarSolicitacao(idempotentRequest, transactionId, CANAL_EXTERNO, null))
                .isInstanceOf(RuntimeException.class);

        verify(gestentService, times(1))
                .consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString());
        verify(mapper, times(1)).map(any(SolicitacaoRequestDTO.class));
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando falhar ao consulta do GestEnt")
    void deveLancarExcessaoQuandoFalharConsultaDoGestEnt() {
        var requestDTO = SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos();
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var idempotentRequest = mapIdempotenteRequest(requestDTO);
        var transactionId = idempotentRequest.getTransactionId();

        when(gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString()))
                .thenThrow(AgenciaECooperativaNaoCorrespondentesException.class);
        when(mapper.map(any(SolicitacaoRequestDTO.class))).thenReturn(solicitacao);

        assertThatThrownBy(() -> service.processarSolicitacao(idempotentRequest, transactionId, CANAL_EXTERNO, null))
                .isInstanceOf(AgenciaECooperativaNaoCorrespondentesException.class);

        verify(gestentService, times(1))
                .consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString());
        verify(mapper, times(1)).map(any(SolicitacaoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve agregar respostas de cadastro da solicitacao quando cadastros processados estão concluídos")
    void deveAgregarRespostasDeCadastroDaSolicitacaoQuandoCadastrosProcessadosConcluido() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos();
        agregaResultadoDeSolicitacaoEVerificaResposta(solicitacao, Resultado.CONCLUIDO, false);
    }

    @Test
    @DisplayName("Deve agregar respostas de cadastro da solicitacao quando cadastros processados estão com erro")
    void deveAgregarRespostasDeCadastroDaSolicitacaoQuandoCadastrosProcessadosErro() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosComErro();
        agregaResultadoDeSolicitacaoEVerificaResposta(solicitacao, Resultado.ERRO, true);
    }

    @Test
    @DisplayName("Deve agregar respostas de cadastro da solicitacao quando cadastros processados estão concluídos parcialmente")
    void deveAgregarRespostasDeCadastroDaSolicitacaoQuandoCadastrosProcessadosConcluidoParcialmente() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidosParcialmente();
        agregaResultadoDeSolicitacaoEVerificaResposta(solicitacao, Resultado.CONCLUIDO_PARCIALMENTE, true);
    }

    @Test
    @DisplayName("Não deve agregar respostas de cadastro da solicitacao quando cadastros não estão finalizados")
    void naoDeveAgregarRespostasDeCadastroDaSolicitacaoQuandoCadastrosNaoFinalizados() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidosParcialmente();
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        solicitacao.setResultado(Resultado.EM_PROCESSAMENTO);
        cadastro.setSolicitacao(solicitacao);

        HashMap<String, String> idempotentAsyncRequestHeaders = new HashMap<>();
        var idempotentAsyncRequest = getCadastroIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.countBySolicitacaoIdAndProcessado(anyLong(), anyBoolean()))
                .thenReturn(1L);
        when(solicitacaoRepository.findByIdTransacaoLock(any(BigInteger.class))).thenReturn(Optional.of(solicitacao));

        var solicitacaoDTOIdempotentResponse = service.agregaRespostasDeCadastroDaSolicitacao(idempotentAsyncRequest);

        assertThat(solicitacaoDTOIdempotentResponse.hasValue()).isFalse();
        assertThat(solicitacaoDTOIdempotentResponse.hasEvents()).isFalse();

        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(1)).countBySolicitacaoIdAndProcessado(any(), any());
        verify(solicitacaoRepository, times(1)).findByIdTransacaoLock(solicitacao.getIdTransacao());
        verifyNoInteractions(webhookService);
    }

    @Test
    @DisplayName("Deve processar Callback Webhook de solicitacao finalizada Concluida")
    void deveProcessarCallbackWebhookDeSolicitacaoFinalizadaConcluida() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos();
        processaCallbackWebhook(Status.FINALIZADO, solicitacao);
    }

    @Test
    @DisplayName("Não Deve processar Callback Webhook de solicitacao finalizada Concluida")
    void naoDeveProcessarCallbackWebhookDeSolicitacaoNaoFinalizada() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos();
        processaCallbackWebhook(Status.PROCESSANDO, solicitacao);
    }

    private void processaCallbackWebhook(Status status, Solicitacao solicitacao) {
        solicitacao = solicitacao.toBuilder()
                .status(status)
                .build();

        var solicitacaoAtualizada = solicitacao.toBuilder()
                .webhookHttpStatusCodigo(String.valueOf(WEBHOOK_CODIGO_SUCESSO))
                .status(status)
                .build();

        when(solicitacaoRepository.findByIdTransacao(any(BigInteger.class)))
                .thenReturn(Optional.of(solicitacao));

        if (status == Status.FINALIZADO) {
            when(webhookService.processarRetornoWebhook(any(Configuracao.class), any(Solicitacao.class)))
                    .thenReturn(WEBHOOK_CODIGO_SUCESSO);
            when(solicitacaoRepository.save(any(Solicitacao.class))).thenReturn(solicitacaoAtualizada);
        }

        service.processarCallbackWebhook(solicitacao.getIdTransacao().toString());

        verify(solicitacaoRepository, times(1)).findByIdTransacao(solicitacao.getIdTransacao());

        if (status == Status.FINALIZADO) {
            verify(webhookService, times(1)).processarRetornoWebhook(any(Configuracao.class), any(Solicitacao.class));
            verify(solicitacaoRepository, times(1)).save(solicitacaoAtualizada);
        } else {
            verify(webhookService, never()).processarRetornoWebhook(any(Configuracao.class), any(Solicitacao.class));
            verify(solicitacaoRepository, never()).save(solicitacaoAtualizada);
        }
    }

    private void agregaResultadoDeSolicitacaoEVerificaResposta(Solicitacao solicitacao, Resultado resultado, boolean critica) {
        var cadastro = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidos().getCadastros().getFirst();
        solicitacao.setResultado(Resultado.EM_PROCESSAMENTO);
        cadastro.setSolicitacao(solicitacao);
        HashMap<String, String> idempotentAsyncRequestHeaders = new HashMap<>();
        var idempotentAsyncRequest = getCadastroIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);
        var solicitacaoAtualizada = solicitacao.toBuilder()
                .resultado(resultado)
                .critica(critica)
                .status(Status.FINALIZADO)
                .build();
        var solicitacaoResponseDTO = SolicitacaoResponseDTO.builder()
                .idTransacao(solicitacao.getIdTransacao()
                        .toString())
                .resultado(solicitacaoAtualizada.getResultado().name())
                .status(solicitacaoAtualizada.getStatus().name())
                .build();

        when(cadastroRepository.countBySolicitacaoIdAndProcessado(cadastro.getSolicitacao().getId(), Boolean.FALSE))
                .thenReturn(0L);
        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(mapper.map(any(Solicitacao.class))).thenReturn(solicitacaoResponseDTO);
        when(solicitacaoRepository.findByIdTransacaoLock(solicitacao.getIdTransacao())).thenReturn(Optional.of(solicitacao));
        when(solicitacaoRepository.save(any(Solicitacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var solicitacaoDTOIdempotentResponse = service.agregaRespostasDeCadastroDaSolicitacao(idempotentAsyncRequest);

        assertThat(solicitacaoDTOIdempotentResponse.getValue().resultado()).isEqualTo(resultado.name());
        assertThat(solicitacaoDTOIdempotentResponse.getValue().status()).isEqualTo(Status.FINALIZADO.name());

        verify(cadastroRepository, times(1)).countBySolicitacaoIdAndProcessado(any(), any());
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(mapper, times(1)).map(any(Solicitacao.class));
        verify(solicitacaoRepository, times(1)).findByIdTransacaoLock(solicitacao.getIdTransacao());
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
        verifyNoInteractions(webhookService);
    }

    private static IdempotentAsyncRequest<Cadastro> getCadastroIdempotentAsyncRequest(Cadastro cadastro, HashMap<String, String> idempotentAsyncRequestHeaders) {
        return IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();
    }

    private IdempotentRequest<SolicitacaoRequestDTO> mapIdempotenteRequest(SolicitacaoRequestDTO request) {
        Map<String, String> headers = new HashMap<>();
        headers.put(CABECALHO_INTERNO_TRANSACTION_ID_SOLICITACAO, TRANSACTIONID.toString());
        headers.put(CABECALHO_EXTERNO_CANAL, CANAL_EXTERNO);
        return IdempotentRequest.<SolicitacaoRequestDTO>builder()
                .transactionId(TRANSACTIONID.toString())
                .value(request)
                .headers(headers)
                .build();
    }

    private void prepararMocksParaProcessamentoDeSolicitacao(SolicitacaoResponseDTO expectedResponseDTO, Solicitacao solicitacao) {
        when(gestentService.consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString()))
                .thenReturn("ACA");
        when(mapper.map(any(Solicitacao.class))).thenReturn(expectedResponseDTO);
        when(mapper.map(any(SolicitacaoRequestDTO.class))).thenReturn(solicitacao);
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> {
            Solicitacao argument = invocation.getArgument(0);
            var cadastros = argument.getCadastros();
            IntStream.range(0, cadastros.size()).forEach(i -> cadastros.get((i)).setId((long) i + 1));
            return argument.toBuilder().id(1L).build();
        });
    }

    private void assertSolicitacaoProcessada(IdempotentResponse<SolicitacaoResponseDTO> idempotentResponse, SolicitacaoResponseDTO expectedResponseDTO, SolicitacaoResponseDTO responseDTO) {
        assertThat(idempotentResponse.isErrorResponse()).isFalse();
        assertThat(idempotentResponse.getValue()).isEqualTo(expectedResponseDTO);
        assertThat(responseDTO).isEqualTo(expectedResponseDTO);
        assertThat(responseDTO.status()).isEqualTo(Status.PENDENTE.name());
        assertThat(responseDTO.resultado()).isEqualTo(Resultado.RECEBIDO.name());
        assertThat(responseDTO.cadastros())
                .allMatch(cadastroResponseDTO -> Resultado.RECEBIDO.name().equals(cadastroResponseDTO.situacao()));

        verify(gestentService, times(1))
                .consultaCodigoEntidadeDeCooperativaEAgenciaCorrespondentes(anyString(), anyString());
        verify(mapper, times(1)).map(any(Solicitacao.class));
        verify(mapper, times(1)).map(any(SolicitacaoRequestDTO.class));
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
    }
}