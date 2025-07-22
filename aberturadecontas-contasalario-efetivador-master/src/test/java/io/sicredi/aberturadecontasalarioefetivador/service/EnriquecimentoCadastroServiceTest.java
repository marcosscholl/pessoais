package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.EmailFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.TelefoneFactory;
import io.sicredi.aberturadecontasalarioefetivador.repository.CadastroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnriquecimentoCadastroServiceTest {

    @Mock
    private CadastroAssociadoService cadastroAssociadoService;
    @Mock
    private EmailService emailService;
    @Mock
    private TelefoneService telefoneService;
    @Mock
    private CadastroRepository cadastroRepository;
    @InjectMocks
    private EnriquecimentoCadastroService enriquecimentoCadastroService;
    private Cadastro cadastro;
    private DadosAssociado dadosAssociado;
    private static final String TRANSACTION_ID_SOLICITACAO = "11111";
    private static final String TRANSACTION_ID = "22222";

    @BeforeEach
    void setUp() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        cadastro = solicitacao.getCadastros().getFirst();
        dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
    }

    @Test
    @DisplayName("Deve processar cadastro com email e telefone")
    void deveProcessarCadastroComEmailETelefone() {
        configurarMocks(true, true, true, true);

        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertTrue(resultadoProcessamento.isEmail());
        assertTrue(resultadoProcessamento.isTelefone());
        assertTrue(resultadoProcessamento.isEmailCriado());
        assertTrue(resultadoProcessamento.isTelefoneCriado());
        assertTrue(resultadoProcessamento.getCriticas().isEmpty());

        verify(emailService, times(1)).salvarEmailNovo(Mockito.any(DadosAssociado.class), anyString(), anyString());
        verify(telefoneService, times(1)).salvarNovoTelefone(Mockito.any(DadosAssociado.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve processar cadastro somente com email")
    void deveProcessarCadastroSomenteComEmail() {
        configurarMocks(true, false, true, false);

        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertTrue(resultadoProcessamento.isEmail());
        assertFalse(resultadoProcessamento.isTelefone());
        assertTrue(resultadoProcessamento.isEmailCriado());
        assertFalse(resultadoProcessamento.isTelefoneCriado());
        assertTrue(resultadoProcessamento.getCriticas().isEmpty());

        verify(emailService, times(1)).salvarEmailNovo(Mockito.any(DadosAssociado.class), anyString(), anyString());
        verifyNoInteractions(telefoneService);
    }

    @Test
    @DisplayName("Deve processar cadastro somente com telefone")
    void deveProcessarCadastroSomenteComTelefone() {
        configurarMocks(false, true, false, true);

        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertFalse(resultadoProcessamento.isEmail());
        assertTrue(resultadoProcessamento.isTelefone());
        assertFalse(resultadoProcessamento.isEmailCriado());
        assertTrue(resultadoProcessamento.isTelefoneCriado());
        assertTrue(resultadoProcessamento.getCriticas().isEmpty());

        verify(telefoneService, times(1)).salvarNovoTelefone(Mockito.any(DadosAssociado.class), anyString(), anyString());
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Não deve processar cadastro sem email nem telefone")
    void naoDeveProcessarCadastroSemEmailNemTelefone() {
        cadastro.setEmail(null);
        cadastro.setTelefone(null);
        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertFalse(resultadoProcessamento.isEmail());
        assertFalse(resultadoProcessamento.isTelefone());
        assertFalse(resultadoProcessamento.isEmailCriado());
        assertFalse(resultadoProcessamento.isTelefoneCriado());
        assertTrue(resultadoProcessamento.getCriticas().isEmpty());

        verifyNoInteractions(cadastroAssociadoService);
        verifyNoInteractions(emailService);
        verifyNoInteractions(telefoneService);
    }

    @Test
    @DisplayName("Deve retornar resultado com crítica quando ocorrer erro ao salvar email")
    void deveRetornarResultadoComCriticaQuandoOcorrerErroAoSalvarEmail() {
        configurarMocks(true, false, false, false);

        doThrow(new RuntimeException("Erro ao salvar email"))
                .when(emailService).salvarEmailNovo(any(), anyString(), anyString());
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));

        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertTrue(resultadoProcessamento.isEmail());
        assertFalse(resultadoProcessamento.isEmailCriado());
        assertFalse(resultadoProcessamento.isTelefone());
        assertFalse(resultadoProcessamento.isTelefoneCriado());
        assertFalse(resultadoProcessamento.getCriticas().isEmpty());
        assertTrue(resultadoProcessamento.getCriticas().getFirst().contains("Erro ao salvar email"));

        verify(emailService, times(1)).salvarEmailNovo(Mockito.any(DadosAssociado.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar resultado com crítica quando ocorrer erro ao salvar telefone")
    void deveRetornarResultadoComCriticaQuandoOcorrerErroAoSalvarTelefone() {
        configurarMocks(false, true, false, false);

        doThrow(new RuntimeException("Erro ao salvar telefone"))
                .when(telefoneService).salvarNovoTelefone(any(), anyString(), anyString());

        var resultadoProcessamento = enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertFalse(resultadoProcessamento.isEmail());
        assertTrue(resultadoProcessamento.isTelefone());
        assertFalse(resultadoProcessamento.isEmailCriado());
        assertFalse(resultadoProcessamento.isTelefoneCriado());
        assertFalse(resultadoProcessamento.getCriticas().isEmpty());
        assertTrue(resultadoProcessamento.getCriticas().getFirst().contains("Erro ao salvar telefone"));

        verify(telefoneService, times(1)).salvarNovoTelefone(Mockito.any(DadosAssociado.class), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve retornar resultado com crítica quando ocorrer erro ao consultar dados de cadastro do associado")
    void deveRetornarResultadoComCriticaQuandoOcorrerErroAoConsultarDadosDeCadastroDoAssociado() {
        doThrow(new RuntimeException("Erro no CadastroAssociadoService"))
                .when(cadastroAssociadoService).consultarDadosAssociado(anyString());
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));

        var resultadoProcessamento =
                enriquecimentoCadastroService.processarCadastro(cadastro, TRANSACTION_ID_SOLICITACAO, TRANSACTION_ID);

        assertTrue(resultadoProcessamento.isEmail());
        assertFalse(resultadoProcessamento.isEmailCriado());
        assertTrue(resultadoProcessamento.isTelefone());
        assertFalse(resultadoProcessamento.isTelefoneCriado());
        assertFalse(resultadoProcessamento.getCriticas().isEmpty());
        assertTrue(resultadoProcessamento.getCriticas().getFirst().contains("Erro ao consultar os dados do CPF 21180506073 : Erro no CadastroAssociadoService"));

        verifyNoInteractions(emailService);
        verifyNoInteractions(telefoneService);
    }

    private void configurarMocks(boolean emailCadastrado, boolean telefoneCadastrado, boolean criarEmail, boolean criarTelefone) {
        when(cadastroAssociadoService.consultarDadosAssociado(anyString()))
                .thenReturn(ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro));
        when(cadastroRepository.findById(anyLong())).thenReturn(Optional.of(cadastro));

        if (emailCadastrado) {
            when(emailService.consultarEmail(Mockito.any(DadosAssociado.class)))
                    .thenReturn(EmailFactory.consultarEmailResponseSemEmail());
            if (criarEmail) {
                when(emailService.salvarEmailNovo(Mockito.any(DadosAssociado.class), anyString(), anyString()))
                        .thenReturn(EmailFactory.salvarEmailNovoResponse(dadosAssociado));
            }
        } else {
            cadastro.setEmail(null);
        }

        if (telefoneCadastrado) {
            when(telefoneService.consultarTelefones(Mockito.any(DadosAssociado.class)))
                    .thenReturn(TelefoneFactory.consultarTelefoneResponseSemResultado());
            if (criarTelefone) {
                when(telefoneService.salvarNovoTelefone(Mockito.any(DadosAssociado.class), anyString(), anyString()))
                        .thenReturn(TelefoneFactory.salvarTelefoneResponse(dadosAssociado));
            }
        } else {
            cadastro.setTelefone(null);
        }
    }
}