package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;

import br.com.sicredi.contasalario.ejb.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebserviceException;
import io.sicredi.aberturadecontasalarioefetivador.factories.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaSalarioContasServiceClientTest {

    private static final String AGENCIA = "0167";
    private static final String NUMERO_CONTA = "903677";

    private ContaSalarioContasServiceClient contaSalarioContasServiceClient;
    @Mock
    private WebServiceTemplate webServiceTemplate;
    @Mock
    private Jaxb2Marshaller marshaller;

    @BeforeEach
    void setUp() {
        contaSalarioContasServiceClient = new ContaSalarioContasServiceClient(marshaller, "URI", 1000, 2);
        contaSalarioContasServiceClient.setWebServiceTemplate(webServiceTemplate);
    }

    @Test
    @DisplayName("Deve consultar conta salário")
    void deveConsultarContaSalario() {
        ConsultarSaldoContaSalarioResponse consultarSaldoContaSalarioResponse =
                ConsultarSaldoContaSalarioFactory.consultarSaldoContaSalarioResponse();

        var response = new ObjectFactory().createConsultarSaldoContaSalarioResponse(consultarSaldoContaSalarioResponse);

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class)))
                .thenReturn(response);

        ConsultarSaldoContaSalarioResponse retornado =
                contaSalarioContasServiceClient.consultarContaSalario(AGENCIA, NUMERO_CONTA);

        assertThat(retornado).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario()).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO()).isNotNull();
        assertThat(retornado.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO().getNumeroContaSalario()).isEqualTo(NUMERO_CONTA);

    }

    @Test
    @DisplayName("Deve efetuar retry 2 vezes após receber erro na requisição ao webservice")
    void deveEfetuarRetryAoReceberErroWebService() {

        when(webServiceTemplate.marshalSendAndReceive(any(String.class), any(Object.class)))
                .thenThrow(WebserviceException.class);

        contaSalarioContasServiceClient.consultarContaSalario(AGENCIA, NUMERO_CONTA);

        verify(webServiceTemplate, times(2)).marshalSendAndReceive(any(String.class), any(Object.class));
    }
}