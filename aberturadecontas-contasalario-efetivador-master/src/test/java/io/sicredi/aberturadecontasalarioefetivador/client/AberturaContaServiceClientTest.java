package io.sicredi.aberturadecontasalarioefetivador.client;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice.AberturaContaServiceClient;
import io.sicredi.aberturadecontasalarioefetivador.factories.GetFontePagadoraFactory;
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
class AberturaContaServiceClientTest {

    private AberturaContaServiceClient aberturaContaServiceClient;

    @Mock
    private WebServiceTemplate webServiceTemplate;

    @Mock
    private Jaxb2Marshaller marshaller;

    @BeforeEach
    void setUp() {
        aberturaContaServiceClient = new AberturaContaServiceClient(marshaller, "URI");
        aberturaContaServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve consultar fonte pagadora com sucesso")
    void deveConsultarFontePagadoraERetornarQuandoEncontrar() {
        var solicitacao = SolicitacaoFactory.solicitacaoPendenteDoisCadastros(SolicitacaoRequestDTOFactory.solicitacaoDoisCadastrosCompletos());
        var fontesPagadoras = GetFontePagadoraFactory.getFontesPagadoras(solicitacao);
        var fontesPagadorasResponseValido = GetFontePagadoraFactory.getFontesPagadorasResponseValido(solicitacao);
        var response = new ObjectFactory().createGetFontesPagadorasResponse(fontesPagadorasResponseValido);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        var retornado = aberturaContaServiceClient.consultarFontesPagadoras(fontesPagadoras);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getFontesPagadoras()).isNotNull();
        assertThat(retornado.getFontesPagadoras().getFontePagadora()).isNotNull();
        assertThat(retornado.getFontesPagadoras().getFontePagadora()).hasSize(1);
        assertThat(retornado.getFontesPagadoras().getFontePagadora().getFirst().getCnpj()).isEqualTo(solicitacao.getCnpjFontePagadora());
        assertThat(retornado.getFontesPagadoras().getFontePagadora().getFirst().getCodigo()).isEqualTo(solicitacao.getCodConvenioFontePagadora());
    }
}