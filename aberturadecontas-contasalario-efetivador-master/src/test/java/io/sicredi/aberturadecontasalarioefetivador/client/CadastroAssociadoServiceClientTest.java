package io.sicredi.aberturadecontasalarioefetivador.client;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.CadastroAssociadoServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
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
class CadastroAssociadoServiceClientTest {

    private CadastroAssociadoServiceClient cadastroAssociadoServiceClient;
    @Mock
    private WebServiceTemplate webServiceTemplate;
    @Mock
    private Jaxb2Marshaller marshaller;

    @BeforeEach
    void setUp() {
        cadastroAssociadoServiceClient = new CadastroAssociadoServiceClient(marshaller, "URI");
        cadastroAssociadoServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve consultar dados do associado")
    void deveConsultarDadosAssociado() {
        Solicitacao solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());

        var cadastro = solicitacao.getCadastros().getFirst();
        var consultarDadosAssociado = ConsultarDadosAssociadoFactory.consultarDadosAssociado(cadastro);
        var consultarDadosAssociadoResponse = ConsultarDadosAssociadoFactory.consultarDadosAssociadoResponse(cadastro);
        var response = new ObjectFactory().createConsultarDadosAssociadoResponse(consultarDadosAssociadoResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        var retornado = cadastroAssociadoServiceClient.consultarDadosAssociado(consultarDadosAssociado);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getOutConsultarDadosAssociado()).isNotNull();
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos()).isNotNull();
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos()).hasSize(1);
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getNomAssociado()).isEqualTo(cadastro.getNome());
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getNroDocumento()).isEqualTo(cadastro.getCpf());
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getDatNascimento().getYear()).isEqualTo(cadastro.getDataNascimento().getYear());
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getDatNascimento().getMonth()).isEqualTo(cadastro.getDataNascimento().getMonthValue());
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getDatNascimento().getDay()).isEqualTo(cadastro.getDataNascimento().getDayOfMonth());
        assertThat(retornado.getOutConsultarDadosAssociado().getElementos().getFirst().getOidPessoa()).isEqualTo(Long.parseLong(cadastro.getCpf()));

    }
}