package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.client.bureaurf.BureauRFClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.DadosRF;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoCritica;
import io.sicredi.aberturadecontasalarioefetivador.factories.BureauRFDTOFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.DadosRFFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.mapper.DadosRFMapper;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BureauRFServiceTest {
    @Mock
    BureauRFClient bureauRFClient;
    @Mock
    CadastroRepository cadastroRepository;
    @Mock
    DadosRFMapper dadosRFMapper;
    @Mock
    MetricService metricService;
    @InjectMocks
    BureauRFService bureauRFService;
    private static final String TRANSACTION_ID = "1234567890";
    private static final String RESPONSE_TOPIC = "cadastroTopic";
    private static final String RESPONSE_ERROR_TOPIC = "erroCadastroTopic";

    @Test
    @DisplayName("Deve buscar os dados do cliente maior de idade no Bureau da RF e gerar a resposta de sucesso sem criticas")
    void deveBuscarDadosDoClienteMaiorDeIdadeNoBureauRFEGerarRespostaDeSucessoSemCriticas(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.maiorDeIdadeRegular();
        var dadosRfEsperado = DadosRFFactory.maiorDeIdadeRegular();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenTitular(cadastro, bureauRFDTO, dadosRfEsperado);

        var idempotentResponse = bureauRFService.processarSolicitacaoBureauRF(idempotentAsyncRequest);
        var cadastroResposta = idempotentResponse.getValue();

        assertThat(idempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroResposta).isNotNull();
        assertThat(cadastroResposta.getDadosRF()).isNotNull();
        assertThat(cadastroResposta.getCriticas()).isNotNull().isEmpty();
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(1);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente no Bureau da RF e gerar a resposta de sucesso com criticas de nome, data de nascimento e sexo diferentes do informado")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeSucessoComCriticasDeNomeEDataNascimentoDiferentesESexo(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.maiorDeIdadeRegular();
        var dadosRfEsperado = DadosRFFactory.maiorDeIdadeRegular();
        cadastro.setNome("Nome Diferente");
        cadastro.setFlgSexo("F");
        cadastro.setDataNascimento(bureauRFDTO.dataNascimento().minusYears(5));
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenTitular(cadastro, bureauRFDTO, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var idempotentResponse = bureauRFService.processarSolicitacaoBureauRF(idempotentAsyncRequest);
        var cadastroResposta = idempotentResponse.getValue();

        assertThat(idempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroResposta).isNotNull();
        assertThat(cadastroResposta.getDadosRF()).isNotNull();
        assertThat(cadastroResposta.getCriticas()).isNotNull().isNotEmpty();
        assertThat(cadastroResposta.getCriticas()).allSatisfy(critica -> {
            assertThat(critica.getTipo()).isEqualTo(TipoCritica.INFORMATIVO);
            assertThat(critica.getCodigo()).isIn("RFB004", "RFB005", "RFB006");
            assertThat(critica.getDescricao()).isIn("Nome informado no cadastro diferente do nome na Receita Federal.",
                    "Data de nascimento informada no cadastro diferente da data de nascimento na Receita Federal.",
                    "Sexo do cliente informado no cadastro diferente do sexo na Receita Federal.");
        });
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(1);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente no Bureau da RF e gerar a resposta de sucesso com criticas de nome, data de nascimento e sexo diferentes do informado quando forem nulos")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeSucessoComCriticasDeNomeEDataNascimentoDiferentesESexoQuandoCamposSaoNulos(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMenorDeIdadeComRepresentante();
        solicitacao.getCadastros().forEach(cadastro -> {
            cadastro.setNome("");
            cadastro.setFlgSexo("");
            cadastro.setDataNascimento(LocalDate.now());
            cadastro.getRepresentante().setNome("");
        });
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.maiorDeIdadeRegular();

        bureauRFDTO = bureauRFDTO.toBuilder()
                .dataNascimento(solicitacao.getCadastros().getFirst().getDataNascimento()).build();
        var bureauRFDTORepresentante = BureauRFDTOFactory.maiorDeIdadeRegular();

        var dadosRfEsperado = DadosRFFactory.maiorDeIdadeRegular();

        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        when(bureauRFClient.consultaCPF(eq("48606049034"), anyString(), anyString())).thenReturn(bureauRFDTORepresentante);
        when(bureauRFClient.consultaCPF(eq("21180506073"), anyString(), anyString())).thenReturn(bureauRFDTO);
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(dadosRFMapper.map(any(BureauRFDTO.class))).thenReturn(dadosRfEsperado);
        when(cadastroRepository.save(any(Cadastro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var idempotentResponse = bureauRFService.processarSolicitacaoBureauRF(idempotentAsyncRequest);
        var cadastroResposta = idempotentResponse.getValue();

        assertThat(idempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroResposta).isNotNull();
        assertThat(cadastroResposta.getCriticas()).allSatisfy(critica -> {
            assertThat(critica.getTipo()).isEqualTo(TipoCritica.INFORMATIVO);
            assertThat(critica.getCodigo()).isIn("RFB004", "RFB005", "RFB006");
            assertThat(critica.getDescricao()).isIn("Nome informado no cadastro diferente do nome na Receita Federal.",
                    "Data de nascimento informada no cadastro diferente da data de nascimento na Receita Federal.",
                    "Sexo do cliente informado no cadastro diferente do sexo na Receita Federal.");
        });
        assertThat(cadastroResposta.getDadosRF()).isNotNull();
        assertThat(cadastroResposta.getCriticas()).isNotNull();
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
    }
    @Test
    @DisplayName("Deve buscar os dados do cliente no Bureau da RF e gerar a resposta de erro com crítica de situação irregular na RF")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCriticaDeSituacaoIrregularNaRF(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.maiorDeIdadeIrregular();
        var dadosRfEsperado = DadosRFFactory.maiorDeIdadeIrregular();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenTitular(cadastro, bureauRFDTO, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var cadastroResposta = processaSolicitacaoBureauRFEAsserts(idempotentAsyncRequest,
                "RFB001", "CPF em situação irregular na base Receita Federal.");

        assertThat(cadastroResposta.getSituacao()).isEqualTo(Resultado.ERRO);
        assertThat(cadastroResposta.isProcessado()).isTrue();
        assertThat(cadastroResposta.isEfetivado()).isFalse();
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(1);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente no Bureau da RF e gerar a resposta de erro com crítica de menor de idade sem dados do representante legal")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCriticaDeMenorSemDadosDoRepresentante(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMenorDeIdadeSemRepresentante();
        var cadastro = solicitacao.getCadastros().getFirst();
        deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCritica(cadastro);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente e do representante no Bureau da RF e gerar a resposta de erro com crítica de representante com situação irregular na RF")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCriticaRepresentanteIrregularNaRF(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMenorDeIdadeComRepresentante();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.menorDeIdadeRegular();
        var bureauRFDTORepresentanteEsperado = BureauRFDTOFactory.maiorDeIdadeIrregular();
        var dadosRfEsperado = DadosRFFactory.menorDeIdadeRegular();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenRepresentante(cadastro, bureauRFDTO, bureauRFDTORepresentanteEsperado, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var cadastroResposta = processaSolicitacaoBureauRFEAsserts(idempotentAsyncRequest,
                "RFB001", "CPF em situação irregular na base Receita Federal.");
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(2);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente e do representante no Bureau da RF e gerar a resposta de erro com crítica de representante menor de idade")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCriticaRepresentanteMenorDeIdade(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMenorDeIdadeComRepresentante();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.menorDeIdadeRegular();
        var dadosRfEsperado = DadosRFFactory.menorDeIdadeRegular();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenRepresentante(cadastro, bureauRFDTO, bureauRFDTO, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var cadastroResposta = processaSolicitacaoBureauRFEAsserts(idempotentAsyncRequest,
                "RFB003", "Representante legal informado é menor de idade.");
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(2);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente e do representante no Bureau da RF e gerar a resposta de erro com crítica de representante sem cpf (sem representante)")
    void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCriticaRepresentanteSemCPF(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMenorDeIdadeComRepresentante();
        var cadastro = solicitacao.getCadastros().getFirst();
        cadastro.getRepresentante().setCpf("");
        deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCritica(cadastro);
    }

    @Test
    @DisplayName("Deve buscar os dados do cliente no Bureau da RF e gerar a resposta de sucesso com critica de representante não cadastrado")
    void deveBuscarDadosDoClienteNoBureauRFEGerarRespostaDeSucessoComCriticaInformativaDeRepresentanteNaoCadastrado(){
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteMaiorDeIdadeComRepresentante();
        var cadastro = solicitacao.getCadastros().getFirst();
        var bureauRFDTO = BureauRFDTOFactory.maiorDeIdadeRegular();

        var dadosRfEsperado = DadosRFFactory.maiorDeIdadeRegular();

        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenTitular(cadastro, bureauRFDTO, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var idempotentResponse = bureauRFService.processarSolicitacaoBureauRF(idempotentAsyncRequest);
        var cadastroResposta = idempotentResponse.getValue();
        var critica = cadastroResposta.getCriticas().stream().findFirst();

        assertThat(critica).isPresent();
        assertThat(critica.get().getTipo()).isEqualTo(TipoCritica.INFORMATIVO);
        assertThat(critica.get().getCodigo()).isEqualTo("RFB008");
        assertThat(critica.get().getDescricao()).isEqualTo("Titular em maioridade, representante não cadastrado.");
        assertThat(idempotentResponse.isErrorResponse()).isFalse();
        assertThat(cadastroResposta).isNotNull();
        assertThat(cadastroResposta.getDadosRF()).isNotNull();
        assertThat(cadastroResposta.getCriticas()).isNotNull();
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(1);
    }

    private void deveBuscarDadosNoBureauDaRFEGerarRespostaDeErroComCritica(Cadastro cadastro) {
        var bureauRFDTO = BureauRFDTOFactory.menorDeIdadeRegular();
        var dadosRfEsperado = DadosRFFactory.menorDeIdadeRegular();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = getIdempotentAsyncRequest(cadastro, idempotentAsyncRequestHeaders);

        mockWhenTitular(cadastro, bureauRFDTO, dadosRfEsperado);
        doNothing().when(metricService).incrementCounter(anyString());

        var cadastroResposta = processaSolicitacaoBureauRFEAsserts(idempotentAsyncRequest,
                "RFB002", "Cadastro de menor de idade informado sem representante legal.");
        assertThatCadastroRespostaXBureauRF(dadosRfEsperado, cadastroResposta);
        verifyMocksTimesCalled(1);
    }

    private static IdempotentAsyncRequest<Cadastro> getIdempotentAsyncRequest(Cadastro cadastro, Map<String, String> idempotentAsyncRequestHeaders) {
        return IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(TRANSACTION_ID)
                .responseTopic(RESPONSE_TOPIC)
                .responseErrorTopic(RESPONSE_ERROR_TOPIC)
                .build();
    }

    private void mockWhenTitular(Cadastro cadastro, BureauRFDTO bureauRFDTO, DadosRF dadosRfEsperado) {
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));
        when(bureauRFClient.consultaCPF(anyString(), anyString(), anyString())).thenReturn(bureauRFDTO);
        when(dadosRFMapper.map(any(BureauRFDTO.class))).thenReturn(dadosRfEsperado);
        when(cadastroRepository.save(any(Cadastro.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void mockWhenRepresentante(Cadastro cadastro, BureauRFDTO bureauRFDTO, BureauRFDTO bureauRFDTORepresentanteEsperado, DadosRF dadosRfEsperado) {
        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(bureauRFClient.consultaCPF(eq(cadastro.getCpf()), anyString(), anyString())).thenReturn(bureauRFDTO);
        when(bureauRFClient.consultaCPF(eq(cadastro.getRepresentante().getCpf()), anyString(), anyString())).thenReturn(bureauRFDTORepresentanteEsperado);
        when(dadosRFMapper.map(any(BureauRFDTO.class))).thenReturn(dadosRfEsperado);
        when(cadastroRepository.save(any(Cadastro.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }


    private Cadastro processaSolicitacaoBureauRFEAsserts(IdempotentAsyncRequest<Cadastro> idempotentAsyncRequest,
                                                         String codigoCriticaEsperado, String descricaoCriticaEsperada) {
        var idempotentResponse = bureauRFService.processarSolicitacaoBureauRF(idempotentAsyncRequest);
        Cadastro cadastroResposta = idempotentResponse.getValue();

        assertThat(idempotentResponse.isErrorResponse()).isTrue();
        assertThat(cadastroResposta).isNotNull();
        assertThat(cadastroResposta.getDadosRF()).isNotNull();
        assertThat(cadastroResposta.getCriticas()).isNotNull().isNotEmpty().hasSize(1);
        assertThat(cadastroResposta.getCriticas()).allSatisfy(critica -> {
            assertThat(critica.getTipo()).isEqualTo(TipoCritica.BLOQUEANTE);
            assertThat(critica.getCodigo()).isEqualTo(codigoCriticaEsperado);
            assertThat(critica.getDescricao()).isEqualTo(descricaoCriticaEsperada);
        });
        return cadastroResposta;
    }

    private static void assertThatCadastroRespostaXBureauRF(DadosRF dadosRfEsperado, Cadastro cadastroResposta) {
        assertThat(cadastroResposta.getDadosRF().getNome()).isEqualTo(dadosRfEsperado.getNome());
        assertThat(cadastroResposta.getDadosRF().getSexo()).isEqualTo(dadosRfEsperado.getSexo());
        assertThat(cadastroResposta.getDadosRF().getDataNascimento()).isEqualTo(dadosRfEsperado.getDataNascimento());
        assertThat(cadastroResposta.getDadosRF().getCodigoSituacaoCadastral()).isEqualTo(dadosRfEsperado.getCodigoSituacaoCadastral());
        assertThat(cadastroResposta.getDadosRF().getDescSituacaoCadastral()).isEqualTo(dadosRfEsperado.getDescSituacaoCadastral());
        assertThat(cadastroResposta.getDadosRF().getSituacaoCadastral()).isEqualTo(dadosRfEsperado.getSituacaoCadastral());
    }

    private void verifyMocksTimesCalled(int bureauRFClientExpectedInvokations) {
        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(bureauRFClient, times(bureauRFClientExpectedInvokations))
                .consultaCPF(anyString(), anyString(), anyString());
        verify(dadosRFMapper, times(1)).map(any(BureauRFDTO.class));
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
    }
}