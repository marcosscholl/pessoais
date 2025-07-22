package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.entities.Critica;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.TipoCritica;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentAsyncRequest;
import io.sicredi.engineering.libraries.idempotent.transaction.IdempotentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DltEventServiceTest {

    @Mock
    private CadastroRepository cadastroRepository;

    @InjectMocks
    DltEventService dltEventService;

    @Test
    @DisplayName("Deve processar evento de erro de solicitação no BureauRF")
    void deveProcessarEventoDeErroDeSolicitacaoNoBureauRF() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var critica = Critica.builder()
                .codigo("RFB007")
                .descricao("Receita Federal indisponível.")
                .tipo(TipoCritica.BLOQUEANTE)
                .build();
        deveProcessarErroDeSolicitacao(critica, cadastro, 1);
    }

    @Test
    @DisplayName("Deve processar evento de erro de solicitação no BureauRF e manter criticas já existentes")
    void deveProcessarEventoDeErroDeSolicitacaoNoBureauRFEManterCriticasJaExistentes() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidosParcialmente();
        var cadastro = solicitacao.getCadastros().getFirst();
        var critica = Critica.builder()
                .codigo("RFB007")
                .descricao("Receita Federal indisponível.")
                .tipo(TipoCritica.BLOQUEANTE)
                .build();
        deveProcessarErroDeSolicitacao(critica, cadastro, 2);
    }

    @Test
    @DisplayName("Deve processar evento de erro de processamento de solicitacao")
    void deveProcessarEventoDeErroDeProcessamentoDeSolicitacao() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        cadastro.setCriticas(null);
        var critica = Critica.builder()
                .codigo("CCS001")
                .descricao("Erro no cadastro de conta salário.")
                .tipo(TipoCritica.BLOQUEANTE)
                .build();
        deveProcessarErroDeSolicitacao(critica, cadastro, 1);
    }

    @Test
    @DisplayName("Deve retornar NotFoundException quando não encontrar cadastro na base de dados para evento de DLT do BureauRF")
    void deveRetornarNotFoundExceptionQuandoNaoEncontrarCadastroNaBaseDeDadosParaEventoDLTBureauRF() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastrosConcluidosParcialmente();
        var cadastro = solicitacao.getCadastros().getFirst();
        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId(solicitacao.getIdTransacao().toString())
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                dltEventService.processarErroSolicitacaoBureauRF(idempotentAsyncRequest))
                .isInstanceOf(NotFoundException.class);

        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(0)).save(any(Cadastro.class));
    }

    private void deveProcessarErroDeSolicitacao(Critica criticaEsperada, Cadastro cadastro, int numCriticasEsperadas) {
        var criticas = new HashSet<Critica>();
        criticas.add(criticaEsperada);

        if(Objects.nonNull(cadastro.getCriticas()) && !cadastro.getCriticas().isEmpty()){
            criticas.addAll(cadastro.getCriticas());
        }

        var cadastroAtualizado = cadastro.toBuilder()
                .efetivado(false)
                .situacao(Resultado.ERRO)
                .processado(true)
                .criticas(criticas)
                .build();

        var idempotentAsyncRequestHeaders = new HashMap<String, String>();
        var idempotentAsyncRequest = IdempotentAsyncRequest
                .<Cadastro>builder()
                .value(cadastro)
                .headers(idempotentAsyncRequestHeaders)
                .transactionId("12345")
                .build();

        when(cadastroRepository.findById(cadastro.getId())).thenReturn(Optional.of(cadastro));
        when(cadastroRepository.save(any(Cadastro.class))).thenReturn(cadastroAtualizado);

        IdempotentResponse<Cadastro> response;
        if("CCS001".equalsIgnoreCase(criticaEsperada.getCodigo())){
            response = dltEventService.processarErroSolicitacaoCadastro(idempotentAsyncRequest);
        }
        else{
            response = dltEventService.processarErroSolicitacaoBureauRF(idempotentAsyncRequest);
        }

        assertThat(response.isErrorResponse()).isTrue();
        assertThat(response.getValue().getCriticas()).isNotEmpty().hasSize(numCriticasEsperadas);
        assertThat(response.getValue().isProcessado()).isTrue();
        assertThat(response.getValue().isEfetivado()).isFalse();
        assertThat(response.getValue().getCriticas()).contains(criticaEsperada);

        verify(cadastroRepository, times(1)).findById(anyLong());
        verify(cadastroRepository, times(1)).save(any(Cadastro.class));
    }
}