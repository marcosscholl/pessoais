package io.sicredi.aberturadecontasalarioefetivador.client.emailservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.EmailFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.SolicitacaoRequestDTOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceClientTest {

    private EmailServiceClient emailServiceClient;
    @Mock
    private WebServiceTemplate webServiceTemplate;
    @Mock
    private Jaxb2Marshaller marshaller;

    @BeforeEach
    void setUp() {
        emailServiceClient = new EmailServiceClient(marshaller, "URI");
        emailServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve consultar o email de cadastro")
    void deveConsultarEmailDeCadastro() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var cadastro = solicitacao.getCadastros().getFirst();
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        var getEmailsResponse = EmailFactory.consultarEmailResponseComEmail(dadosAssociado, cadastro.getEmail());
        var response = new ObjectFactory().createGetEmailsResponse(getEmailsResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        var retornado = emailServiceClient.consultarEmail(EmailFactory.consultarEmailRequest(dadosAssociado));

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail()).hasSize(1);
        assertThat(retornado.getListaEmail().getEmail().getFirst().getEmail()).isNotNull();
        assertThat(retornado.getListaEmail().getEmail().getFirst().getOidTabela()).isNotNull();
    }

    @Test
    @DisplayName("Deve salvar o email")
    void deveSalvarEmail() {
        var dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        var salvarEmailResponse = EmailFactory.salvarEmailNovoResponse(dadosAssociado);
        var response = new ObjectFactory().createSalvarEmailResponse(salvarEmailResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        var retornado = emailServiceClient.salvarEmail(EmailFactory.salvarEmailNovo(dadosAssociado));

        assertThat(retornado).isNotNull();
        assertThat(retornado.getEmail()).isNotNull();
        assertThat(retornado.getEmail().getEmail()).isNotNull();
        assertThat(retornado.getEmail().getOidTabela()).isEqualTo(dadosAssociado.getOidPessoa()+1);
        assertThat(retornado.getEmail().getTipo()).isEqualTo("P");
    }
}