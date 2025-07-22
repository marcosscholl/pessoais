package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice;

import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalarioResponse;
import br.com.sicredi.mua.cada.business.server.ejb.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.entities.Cadastro;
import io.sicredi.aberturadecontasalarioefetivador.factories.AberturaContaCoexistenciaServiceFactory;
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
class AberturaContaCoexistenciaServiceClientTest {

    private AberturaContaCoexistenciaServiceClient aberturaContaCoexistenciaServiceClient;
    @Mock
    private WebServiceTemplate webServiceTemplate;
    @Mock
    private Jaxb2Marshaller marshaller;

    @BeforeEach
    void setUp() {
        aberturaContaCoexistenciaServiceClient = new AberturaContaCoexistenciaServiceClient(marshaller, "URI");
        aberturaContaCoexistenciaServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve consultar conta salário")
    void deveConsultarContaSalario() {
        var getContaSalarioResponse = AberturaContaCoexistenciaServiceFactory.consultaContaSalarioEncontrada();
        var response = new ObjectFactory().createGetContaSalarioResponse(getContaSalarioResponse);
        var getContaSalario =
                AberturaContaCoexistenciaServiceFactory.consultaContaSalario(Cadastro.builder().cpf("00000000000")
                .oidPessoa(12345L)
                .build());

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        GetContaSalarioResponse retornado = aberturaContaCoexistenciaServiceClient.consultarContaSalario(getContaSalario);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getReturn()).isNotNull();
        assertThat(retornado.getReturn().getConta()).isNotNull();
    }

    @Test
    @DisplayName("Deve consultar instituição financeira")
    void deveConsultarInstituicaoFinanceira() {
        var getInstituicaoFinanceiraResponse = AberturaContaCoexistenciaServiceFactory.criarGetInstituicaoFinanceiraResponse();
        var response = new ObjectFactory().createGetInstituicaoFinanceiraResponse(getInstituicaoFinanceiraResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class))).thenReturn(response);

        var retornado = aberturaContaCoexistenciaServiceClient.consultarInstituicaoFinanceira();

        assertThat(retornado).isNotNull();
        assertThat(retornado.getReturn()).isNotEmpty();
        assertThat(retornado.getReturn().getFirst().getCodigoBanco()).isEqualTo("299");
        assertThat(retornado.getReturn().getFirst().getNomeBanco()).isEqualTo("BCO AFINZ S.A. - BM");
    }
}