package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.GetTelefonesResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.ObjectFactory;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.SalvarTelefone;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.SalvarTelefoneResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice.dto.GetTelefonesBuilder;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConsultarDadosAssociadoFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.TelefoneFactory;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
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
class TelefoneServiceClientTest {

    private TelefoneServiceClient telefoneServiceClient;

    @Mock
    Jaxb2Marshaller marshaller;

    @Mock
    TelefoneServiceConfiguration telefoneServiceConfiguration;

    @Mock
    private WebServiceTemplate webServiceTemplate;

    @BeforeEach
    void setUp() {
        telefoneServiceClient = new TelefoneServiceClient(marshaller, "URI");
        telefoneServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    void deveriaConsultarTelefonesERetornarQuandoEncontrar() {
        DadosAssociado dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();

        GetTelefonesBuilder getTelefones = GetTelefonesBuilder.builder()
                .oidPessoa(dadosAssociado.getOidPessoa())
                .build();
        GetTelefonesResponse getTelefonesResponse = TelefoneFactory.consultarTelefoneResponseComResultado(dadosAssociado);

        JAXBElement<GetTelefonesResponse> response = new ObjectFactory().createGetTelefonesResponse(getTelefonesResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        GetTelefonesResponse retornado = telefoneServiceClient.consultarTelefones(getTelefones);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getListaTelefone()).isNotNull();
        assertThat(retornado.getListaTelefone().getTelefone()).isNotNull();
        assertThat(retornado.getListaTelefone().getTelefone()).hasSize(1);
    }

    @Test
    void deveriaSalvarTelefone() {
        DadosAssociado dadosAssociado = ConsultarDadosAssociadoFactory.dadosAssociadoPrimeiro();
        SalvarTelefone salvarTelefone = TelefoneFactory.salvarTelefone(dadosAssociado);
        SalvarTelefoneResponse salvarTelefoneResponse = TelefoneFactory.salvarTelefoneResponse(dadosAssociado);

        JAXBElement<SalvarTelefoneResponse> response = new ObjectFactory().createSalvarTelefoneResponse(salvarTelefoneResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        SalvarTelefoneResponse retornado = telefoneServiceClient.salvarTelefone(salvarTelefone);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getTelefone()).isNotNull();
        assertThat(retornado.getTelefone().getDdd()).isEqualTo("51");
        assertThat(retornado.getTelefone().getTelefone()).isEqualTo(997649249L);
        assertThat(retornado.getTelefone().getOidTabela()).isEqualTo(dadosAssociado.getOidPessoa()+1);
    }
}