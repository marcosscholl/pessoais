package io.sicredi.aberturadecontasalarioefetivador.service;

import br.com.sicredi.framework.web.spring.exception.NotFoundException;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.GetEmails;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.SalvarEmail;
import io.sicredi.aberturadecontasalarioefetivador.client.emailservice.EmailServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.EmailFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailServiceClient client;
    @InjectMocks
    private EmailService service;
    private static final String EMAIL_NOVO = "novo_mail@mail.com";
    private static final String EMAIL_EXISTENTE = "atualizar_mail@mail.com";
    private static final String BRANCH_CODE = "ACA";
    private static final String EMAIL_PESSOAL = "P";
    private static final String STRING_ERRO_CLIENT_EMAIL = "Erro ao acessar serviço EmailService";

    @Test
    @DisplayName("Deve consultar email e retornar quando encontrado")
    void deveConsultarEmailERetornarQuandoEncontrar() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros();
        var cadastro = solicitacao.getCadastros().getFirst();
        var consultarDadosAssociadoResponse = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro);
        var dadosAssociado = consultarDadosAssociadoResponse.getOutConsultarDadosAssociado().getElementos().getFirst();
        var getEmailsResponse = EmailFactory.consultarEmailResponseComEmail(dadosAssociado, cadastro.getEmail());

        when(client.consultarEmail(any(GetEmails.class))).thenReturn(getEmailsResponse);

        var retornado = service.consultarEmail(dadosAssociado);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail()).isNotNull().hasSize(1);
        assertThat(retornado.getListaEmail().getEmail().getFirst().getEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail().getFirst().getOidTabela()).isNotNull();
        verify(client, times(1)).consultarEmail(any(GetEmails.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao consultar email")
    void deveLancarExceptionQuandoOcorrerErroAoConsultarEmail() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.consultarEmail(any(GetEmails.class))).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.consultarEmail(dadosAssociado))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_EMAIL);

        verify(client, times(1)).consultarEmail(any(GetEmails.class));
    }

    @Test
    @DisplayName("Deve consultar email e retornar vazio quando não encontrá-lo")
    void deveConsultarEmailERetornarVazioQuandoNaoEncontrar() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        var getEmailsResponse = EmailFactory.consultarEmailResponseSemEmail();

        when(client.consultarEmail(any(GetEmails.class))).thenReturn(getEmailsResponse);

        var retornado = service.consultarEmail(dadosAssociado);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail()).isEmpty();

        verify(client, times(1)).consultarEmail(any(GetEmails.class));
    }

    @Test
    @DisplayName("Deve salvar email novo e retornar oidTabela")
    void deveSalvarEmailNovoERetornarOidTabela() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarEmail(any(SalvarEmail.class))).thenReturn(EmailFactory.salvarEmailNovoResponse(dadosAssociado));

        var retornado = service.salvarEmailNovo(dadosAssociado, EMAIL_NOVO, BRANCH_CODE);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getEmail()).isNotNull();
        assertThat(retornado.getEmail().getEmail()).isEqualTo(EMAIL_NOVO);
        assertThat(retornado.getEmail().getOidTabela()).isEqualTo(dadosAssociado.getOidPessoa() + 1);
        assertThat(retornado.getEmail().getTipo()).isEqualTo(EMAIL_PESSOAL);

        verify(client, times(1)).salvarEmail(any(SalvarEmail.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao salvar novo email")
    void deveLancarExceptionQuandoOcorrerErroAoSalvarNovoEmail() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarEmail(any(SalvarEmail.class))).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.salvarEmailNovo(dadosAssociado, EMAIL_NOVO, BRANCH_CODE))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_EMAIL);

        verify(client, times(1)).salvarEmail(any(SalvarEmail.class));
    }

    @Test
    @DisplayName("Deve salvar email existente quando já existir um oidTabela, atualizando oidTabela existente")
    void deveSalvarEmailExistenteQuandoJaExistirUmOidTabelaAtualizandoOIdExistente() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        when(client.salvarEmail(any(SalvarEmail.class))).thenReturn(EmailFactory.salvarEmailExistenteResponse(dadosAssociado));

        var retornado = service.salvarEmailExistente(dadosAssociado, EMAIL_EXISTENTE, dadosAssociado.getOidPessoa() - 1, BRANCH_CODE);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getEmail()).isNotNull();
        assertThat(retornado.getEmail().getEmail()).isEqualTo(EMAIL_EXISTENTE);
        assertThat(retornado.getEmail().getOidTabela()).isEqualTo(dadosAssociado.getOidPessoa() - 1);
        assertThat(retornado.getEmail().getTipo()).isEqualTo(EMAIL_PESSOAL);

        verify(client, times(1)).salvarEmail(any(SalvarEmail.class));
    }

    @Test
    @DisplayName("Deve lançar Exception quando ocorrer erro ao salvar email já existente")
    void deveLancarExceptionQuandoOcorrerErroAoSalvarEmailJaExistente() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        long oidPessoa = dadosAssociado.getOidPessoa() - 1;

        when(client.salvarEmail(any(SalvarEmail.class))).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> service.salvarEmailExistente(dadosAssociado, EMAIL_EXISTENTE, oidPessoa, BRANCH_CODE))
                .isInstanceOf(WebserviceException.class)
                .message().contains(STRING_ERRO_CLIENT_EMAIL);

        verify(client, times(1)).salvarEmail(any(SalvarEmail.class));
    }
}