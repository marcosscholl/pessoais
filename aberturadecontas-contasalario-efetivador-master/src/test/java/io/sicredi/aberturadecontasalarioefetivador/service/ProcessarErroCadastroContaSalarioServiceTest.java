package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.factories.AberturaContaCoexistenciaServiceFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.CadastroAssociadoContasFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.aberturadecontasalarioefetivador.repository.SolicitacaoRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarErroCadastroContaSalarioServiceTest {

    @Mock
    private CadastroRepository cadastroRepository;
    @Mock
    private GestentService gestentService;
    @Mock
    private SolicitacaoRepository solicitacaoRepository;
    @Mock
    private CadastroAssociadoService cadastroAssociadoService;
    @Mock
    private CadastroAssociadoContasService cadastroAssociadoContasService;
    @Mock
    private AberturaContaCoexistenciaService aberturaContaCoexistenciaService;
    @Mock
    private ContaSalarioService contaSalarioService;
    @Mock
    private MetricService metricService;
    @InjectMocks
    private ProcessarErroCadastroContaSalarioService processarErroCadastroContaSalarioService;
    private static final String TRANSACTION_ID_SOLICITACAO_HEADER_KEY = "transactionIdSolicitacao";
    private static final String TRANSACTION_ID_HEADER_KEY = "transactionId";
    private static final String TRANSACTION_ID_HEADER_VALUE = "202409301181156156165196151";
    private static final String ERRO_GENERICO_DESCRICAO = "OSB SERVICE CALLOUT ACTION RECEIVED SOAP FAULT RESPONSE";
    private static final String RESPONSE_TOPIC = "aberturadecontas-contasalario-efetivador-cadastros-resultado-v1";
    private static final long OID_PESSOA = 12345L;
    private static final String CONTA = "903677";
    private static final String BRANCH_CODE = "ACA";
    private Cadastro eventoCadastro;
    private Cadastro cadastro;
    private IdempotentAsyncRequest<Cadastro> cadastroIdempotentAsyncRequest;

    @BeforeEach
    void setup(){
        eventoCadastro = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio().getCadastros().getFirst();
        cadastro = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio().getCadastros().getFirst();
        cadastroIdempotentAsyncRequest = mapIdempotentAsyncRequest(eventoCadastro);
    }


    @Test
    @DisplayName("Não deve processar erro no cadastro caso já esteja com erro e critica não for de interesse")
    void naoDeveProcessarErroCadastroContaSalarioCasoCriticaNaoForDeInteresseESeJaEstiverComoErro() {
        naoDeveProcessarErroCadastroContaSalario(Set.of(Critica.builder().descricao("UNKNOWN").build()));
    }

    @Test
    @DisplayName("Não deve processar erro no cadastro caso já esteja com erro e crítica não for existente")
    void naoDeveProcessarErroCadastroContaSalarioCasoCriticaNaoExistenteESeJaEstiverComoErro() {
        naoDeveProcessarErroCadastroContaSalario(null);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro caso crítica não seja não recuperável e se não estiver com erro")
    void deveProcessarErroCadastroContaSalarioCasoCriticaNaoRecuperavelESeNaoEstiverComoErro() {
        var cadastroErro = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio().getCadastros().getFirst();
        eventoCadastro.setCriticas(null);
        eventoCadastro.setSituacao(Resultado.EM_PROCESSAMENTO);
        cadastro.setCriticas(null);
        cadastro.setSituacao(Resultado.EM_PROCESSAMENTO);
        cadastroErro.setCriticas(null);
        cadastroErro.setEfetivado(false);
        cadastroErro.setSituacao(Resultado.ERRO);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroErro).build();

        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.save(any(Cadastro.class))).thenReturn(cadastroErro);
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isFalse();
        assertThat(retornado.getValue().isProcessado()).isTrue();
        assertThat(retornado.getValue().getSituacao()).isEqualTo(Resultado.ERRO);
        verify(cadastroRepository, times(1)).findById(any());
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
        verifyNoInteractions(metricService);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro e consultar conta salario se crítica for de conta já existente no convênio")
    void deveProcessarErroCadastroContaSalarioEConsultarContaSalarioSeCriticaContaNoConvenio() {
        deveProcessarErroCadastroContaSalarioEConsultarContaSalarioSeCriticaDeInteresse(false);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro e consultar conta salário se crítica for de erro genérico")
    void deveProcessarErroCadastroContaSalarioEConsultarContaSalarioSeCriticaErroGenerico() {
        deveProcessarErroCadastroContaSalarioEConsultarContaSalarioSeCriticaDeInteresse(true);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro e não realizar nenhuma alteração caso não encontra conta no cadastro")
    void deveProcessarErroCadastroContaSalarioENaoFazerNenhumaAlteracaoSeNaoencontrarContaNoCadastro() {
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastro).build();

        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of());
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap()))
                .thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isFalse();
        assertThat(retornado.getValue().getConta()).isNull();
        assertThat(retornado.getValue().getCriticas()).isNotEmpty();

        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
        verifyNoInteractions(aberturaContaCoexistenciaService);
        verifyNoInteractions(cadastroAssociadoService);
        verifyNoInteractions(metricService);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro e não consultarOidPessoa caso preexistente")
    void deveProcessarErroCadastroContaSalarioENaoConsultarOidPessoaSePreexistente() {
        
        eventoCadastro.setOidPessoa(OID_PESSOA);
        
        cadastro.setOidPessoa(OID_PESSOA);
        var cadastroAtualizado = criaCadastroAtualizado();
        var cadastroAssociadoContasDTO = CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada());
        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of(cadastroAssociadoContasDTO));
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.save(any(Cadastro.class))).thenReturn(cadastroAtualizado);
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);

        verify(aberturaContaCoexistenciaService, times(1))
                .consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
        verify(metricService, times(1)).incrementCounter(anyString());
        verifyNoInteractions(cadastroAssociadoService);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro, consultar e atualizar branchCode e solicitação caso não preexistentes")
    void deveProcessarErroCadastroContaSalarioEConsultarEAtualizarBranchCodeSolicitacaoSeNaoPreexistenteASolicitacao() {
        
        
        cadastro.getSolicitacao().setBranchCode(null);
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio();
        solicitacao.setBranchCode(BRANCH_CODE);
        var cadastroAtualizado = criaCadastroAtualizado();
        var cadastroAssociadoContasDTO = CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada());
        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of(cadastroAssociadoContasDTO));
        when(cadastroAssociadoService.consultarCadastroOidPessoa(any(Cadastro.class))).thenReturn(Optional.of(OID_PESSOA));
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.save(any(Cadastro.class))).thenReturn(cadastroAtualizado);
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);
        when(gestentService.consultarCodigoEntidade(anyString(), anyString())).thenReturn(Optional.of(BRANCH_CODE));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenReturn(solicitacao);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);
        assertThat(retornado.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);
        assertThat(retornado.getValue().getCriticas()).isEmpty();

        verify(aberturaContaCoexistenciaService, times(1))
                .consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroAssociadoService, times(1)).consultarCadastroOidPessoa(any(Cadastro.class));
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
        verify(gestentService, times(1)).consultarCodigoEntidade(anyString(), anyString());
        verify(metricService, times(1)).incrementCounter(anyString());
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
    }

    @Test
    @DisplayName("Deve processar erro no cadastro quando houver erro ao consultar conta salário preexistente")
    void deveProcessarErroCadastroContaSalarioQuandoHouverErroAoConsultarContaSalarioPreExistente() {
        
        
        var cadastroAtualizado = criaCadastroAtualizado();
        var cadastroAssociadoContasDTO = CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenThrow(RuntimeException.class);
        when(cadastroAssociadoService.consultarCadastroOidPessoa(cadastro)).thenReturn(Optional.of(OID_PESSOA));
        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of(cadastroAssociadoContasDTO));
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);

        verify(aberturaContaCoexistenciaService, times(1))
                .consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroAssociadoService, times(1)).consultarCadastroOidPessoa(any(Cadastro.class));
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
        verifyNoInteractions(metricService);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro não encontrar BranchCode da Coop e Agência")
    void deveProcessarErroCadastroContaSalarioQuandoNaoEncontrarBranchCodeDaCoopEAgencia() {
        
        eventoCadastro.setOidPessoa(OID_PESSOA);
        
        cadastro.setOidPessoa(OID_PESSOA);
        eventoCadastro.getSolicitacao().setBranchCode(null);
        cadastro.getSolicitacao().setBranchCode(null);
        var cadastroAtualizado = criaCadastroAtualizado();
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);
        when(gestentService.consultarCodigoEntidade(anyString(), anyString())).thenReturn(Optional.empty());

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);

        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
        verify(gestentService, times(1)).consultarCodigoEntidade(anyString(), anyString());
        verifyNoInteractions(aberturaContaCoexistenciaService);
        verifyNoInteractions(cadastroAssociadoContasService);
        verifyNoInteractions(cadastroAssociadoService);
        verifyNoInteractions(metricService);
    }

    @Test
    @DisplayName("Deve processar erro no cadastro e não atualizar conta preexistente caso não encontre OidPessoa")
    void deveProcessarErroNoCadastroENaoAtualizarContaPreexistenteCasoNaoEncontreOidPessoa() {
        deveProcessarErroNoCadastroENaoAtualizarContaPreexistente();
    }

    private void deveProcessarErroNoCadastroENaoAtualizarContaPreexistente() {
        
        
        cadastro.getSolicitacao().setBranchCode(null);
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio();
        solicitacao.setBranchCode(BRANCH_CODE);
        var cadastroAtualizado = criaCadastroAtualizado();
        var cadastroAssociadoContasDTO = CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of(cadastroAssociadoContasDTO));
        when(cadastroAssociadoService.consultarCadastroOidPessoa(any(Cadastro.class))).thenReturn(Optional.empty());
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);
        when(gestentService.consultarCodigoEntidade(anyString(), anyString())).thenReturn(Optional.of(BRANCH_CODE));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenReturn(solicitacao);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);
        assertThat(retornado.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);
        assertThat(retornado.getValue().getCriticas()).isEmpty();

        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroAssociadoService, times(1)).consultarCadastroOidPessoa(any(Cadastro.class));
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(gestentService, times(1)).consultarCodigoEntidade(anyString(), anyString());
        verify(solicitacaoRepository, times(1)).save(any(Solicitacao.class));
        verifyNoInteractions(aberturaContaCoexistenciaService);
        verifyNoInteractions(metricService);
    }

    private void deveProcessarErroCadastroContaSalarioEConsultarContaSalarioSeCriticaDeInteresse(boolean erroGenerico) {

        if (erroGenerico) {
            eventoCadastro.setCriticas(Set.of(Critica.builder().descricao(ERRO_GENERICO_DESCRICAO).build()));
            cadastro.setCriticas(Set.of(Critica.builder().descricao(ERRO_GENERICO_DESCRICAO).build()));
        }

        var cadastroAtualizado = criaCadastroAtualizado();
        var cadastroAssociadoContasDTO = CadastroAssociadoContasFactory.criaCadastroAssociadoContas(false);
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(cadastroAtualizado).build();

        when(aberturaContaCoexistenciaService.consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada());
        when(cadastroAssociadoContasService.buscarContasAssociado(anyString(), anyString())).thenReturn(List.of(cadastroAssociadoContasDTO));
        when(cadastroAssociadoService.consultarCadastroOidPessoa(cadastro)).thenReturn(Optional.of(OID_PESSOA));
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.save(any(Cadastro.class))).thenReturn(cadastroAtualizado);
        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        assertThat(retornado.getValue().isEfetivado()).isTrue();
        assertThat(retornado.getValue().getConta()).isEqualTo(CONTA);
        assertThat(retornado.getValue().getSituacao()).isEqualTo(Resultado.CONCLUIDO);
        assertThat(retornado.getValue().getCriticas()).isEmpty();

        verify(aberturaContaCoexistenciaService, times(1))
                .consultarContaSalario(anyString(), anyString(), anyString(), anyString(), anyLong());
        verify(cadastroAssociadoContasService, times(1)).buscarContasAssociado(anyString(), anyString());
        verify(cadastroAssociadoService, times(1)).consultarCadastroOidPessoa(any(Cadastro.class));
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
        verify(metricService, times(1)).incrementCounter(anyString());
    }

    private void naoDeveProcessarErroCadastroContaSalario(Set<Critica> criticas) {
        var idempotentResponse = IdempotentResponse.<Cadastro>builder().value(eventoCadastro).build();
        eventoCadastro.setCriticas(criticas);

        when(contaSalarioService.processarRespostaSolicitacaoUnitaria(any(), anyString(), anyMap())).thenReturn(idempotentResponse);

        var retornado = processarErroCadastroContaSalarioService.processarErroCadastroContaSalario(cadastroIdempotentAsyncRequest);

        checaNenhumProcessoExecutado(eventoCadastro, retornado);
    }

    private void checaNenhumProcessoExecutado(Cadastro evento, IdempotentResponse<Cadastro> retornado) {
        assertThat(retornado.getValue().isEfetivado()).isEqualTo(evento.isEfetivado());

        verifyNoInteractions(aberturaContaCoexistenciaService);
        verifyNoInteractions(cadastroAssociadoContasService);
        verifyNoInteractions(cadastroAssociadoService);
        verifyNoInteractions(cadastroRepository);
        verifyNoInteractions(gestentService);
        verifyNoInteractions(solicitacaoRepository);
        verifyNoInteractions(metricService);
    }

    private static Cadastro criaCadastroAtualizado() {
        var cadastroEnriquecido = SolicitacaoFactory.solicitacaoPendenteCadastroErroContaExistenteNoConvenio().getCadastros().getFirst();

        cadastroEnriquecido.setOidPessoa(OID_PESSOA);
        cadastroEnriquecido.setConta(CONTA);
        cadastroEnriquecido.setSituacao(Resultado.CONCLUIDO);
        cadastroEnriquecido.setEfetivado(true);
        cadastroEnriquecido.setCriticas(Set.of());
        return cadastroEnriquecido;
    }

    private IdempotentAsyncRequest<Cadastro> mapIdempotentAsyncRequest(Cadastro cadastro) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(TRANSACTION_ID_HEADER_KEY, TRANSACTION_ID_HEADER_VALUE);
        headers.put(TRANSACTION_ID_SOLICITACAO_HEADER_KEY, TRANSACTION_ID_HEADER_VALUE);

        return IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(headers)
                .transactionId(TRANSACTION_ID_HEADER_VALUE.substring(0, TRANSACTION_ID_HEADER_VALUE.length() - 1).concat("3"))
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_TOPIC)
                .build();
    }
}