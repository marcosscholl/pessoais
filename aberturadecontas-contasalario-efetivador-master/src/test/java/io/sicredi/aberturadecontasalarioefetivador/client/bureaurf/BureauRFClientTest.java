package io.sicredi.aberturadecontasalarioefetivador.client.bureaurf;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import feign.FeignException;
import feign.RetryableException;
import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = BureauRFClient.class)
@EnableFeignClients(clients = BureauRFClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
class BureauRFClientTest {

    @Autowired
    BureauRFClient bureauRFClient;
    private static final String CPF = "12345678900";
    private static final String AGENCIA = "1234";
    private static final String COOPERATIVA = "123456";

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar os dados do cliente ao realizar consulta no Bureau da Receita Federal")
    void deveRetornarOsDadosDoClienteAReceitaFederal() {
        stubFor(get(urlMatching("/"+CPF))
                .withHeader(BureauRFClient.AGENCIA_AREA_HEADER, equalTo(AGENCIA))
                .withHeader(BureauRFClient.AUTORIZA_ONLINE_HEADER, equalTo(BureauRFClient.AUTORIZA_ONLINE_VALUE))
                .withHeader(BureauRFClient.COOPERATIVA_HEADER, equalTo(COOPERATIVA))
                .withHeader(BureauRFClient.SISTEMA_ORIGEM_HEADER, equalTo(BureauRFClient.SISTEMA_ORIGEM))
                .withHeader(BureauRFClient.USUARIO_LOGADO_HEADER, equalTo(BureauRFClient.USUARIO_LOGADO_VALUE))
                .willReturn(ResponseDefinitionBuilder.okForJson(BureauRFDTO.builder().build())));
        bureauRFClient.consultaCPF(CPF,AGENCIA, COOPERATIVA);
        verify(1, getRequestedFor(urlMatching("/"+CPF)));
    }

    @Test
    @DisplayName("Deve lançar RetryableException e realizar 3 tentativas de requisição quando receber Http Code 408 como resposta")
    void deveLancarRetryableExceptionERealizarRetriesPara408ComoResposta() {
        stubFor(get(urlMatching("/"+CPF))
                .withHeader(BureauRFClient.AGENCIA_AREA_HEADER, equalTo(AGENCIA))
                .withHeader(BureauRFClient.AUTORIZA_ONLINE_HEADER, equalTo(BureauRFClient.AUTORIZA_ONLINE_VALUE))
                .withHeader(BureauRFClient.COOPERATIVA_HEADER, equalTo(COOPERATIVA))
                .withHeader(BureauRFClient.SISTEMA_ORIGEM_HEADER, equalTo(BureauRFClient.SISTEMA_ORIGEM))
                .withHeader(BureauRFClient.USUARIO_LOGADO_HEADER, equalTo(BureauRFClient.USUARIO_LOGADO_VALUE))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(408)));
        assertThatThrownBy(() -> bureauRFClient.consultaCPF(CPF,AGENCIA, COOPERATIVA))
                .isInstanceOf(RetryableException.class);
        verify(3, getRequestedFor(urlMatching("/"+CPF)));
    }

    @Test
    @DisplayName("Deve lançar RetryableException e realizar 3 tentativas de requisição quando receber Http Code 5xx como resposta")
    void deveLancarRetryableExceptionERealizarRetriesPara5xx() {
        stubFor(get(urlMatching("/"+CPF))
                .withHeader(BureauRFClient.AGENCIA_AREA_HEADER, equalTo(AGENCIA))
                .withHeader(BureauRFClient.AUTORIZA_ONLINE_HEADER, equalTo(BureauRFClient.AUTORIZA_ONLINE_VALUE))
                .withHeader(BureauRFClient.COOPERATIVA_HEADER, equalTo(COOPERATIVA))
                .withHeader(BureauRFClient.SISTEMA_ORIGEM_HEADER, equalTo(BureauRFClient.SISTEMA_ORIGEM))
                .withHeader(BureauRFClient.USUARIO_LOGADO_HEADER, equalTo(BureauRFClient.USUARIO_LOGADO_VALUE))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(503)));
        assertThatThrownBy(() -> bureauRFClient.consultaCPF(CPF,AGENCIA, COOPERATIVA))
                .isInstanceOf(RetryableException.class);
        verify(3, getRequestedFor(urlMatching("/"+CPF)));
    }

    @Test
    @DisplayName("Deve lançar FeignException.Fobidden e não realizar retries")
    void deveLancarFeignExceptionForbiddenENaoRealiarRetries() {
        stubFor(get(urlMatching("/"+CPF))
                .withHeader(BureauRFClient.AGENCIA_AREA_HEADER, equalTo(AGENCIA))
                .withHeader(BureauRFClient.AUTORIZA_ONLINE_HEADER, equalTo(BureauRFClient.AUTORIZA_ONLINE_VALUE))
                .withHeader(BureauRFClient.COOPERATIVA_HEADER, equalTo(COOPERATIVA))
                .withHeader(BureauRFClient.SISTEMA_ORIGEM_HEADER, equalTo(BureauRFClient.SISTEMA_ORIGEM))
                .withHeader(BureauRFClient.USUARIO_LOGADO_HEADER, equalTo(BureauRFClient.USUARIO_LOGADO_VALUE))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(403)));
        assertThatThrownBy(() -> bureauRFClient.consultaCPF(CPF,AGENCIA, COOPERATIVA))
                .isInstanceOf(FeignException.Forbidden.class);
        verify(1, getRequestedFor(urlMatching("/"+CPF)));
    }
}