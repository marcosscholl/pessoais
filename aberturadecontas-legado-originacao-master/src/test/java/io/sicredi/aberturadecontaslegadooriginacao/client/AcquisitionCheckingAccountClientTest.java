package io.sicredi.aberturadecontaslegadooriginacao.client;

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.config.DisableDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = AcquisitionCheckingAccountClient.class)
@EnableFeignClients(clients = AcquisitionCheckingAccountClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class AcquisitionCheckingAccountClientTest {

    @Autowired
    private AcquisitionCheckingAccountClient acquisitionCheckingAccountClient;

    private static final String PATCH ="/checking-account/book-number";
    private static final String ID_PEDIDO_VALUE = "65b2b47cbc9be9327f38c285";
    private static final String ORDER_ID_PARAM = "orderId";
    private static final String COOPERATIVA_VALUE = "0101";
    private static final String COOPERATIVA_PARAM = "coop";


    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar o número da conta no book-number")
    void deveRetornarNumeroDaContaDoBookNumberComSucesso() {
        stubFor(get(urlPathEqualTo(PATCH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("accountLegacy/checking-account/book-number.json")));

        var dadosConta = acquisitionCheckingAccountClient.buscarNumeroConta(COOPERATIVA_VALUE, ID_PEDIDO_VALUE);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String responseBody = serveEvent.getResponse().getBodyAsString();

        verify(1, getRequestedFor(urlPathEqualTo(PATCH)));
        assertEquals(JsonPath.read(responseBody, "$.account"), dadosConta.numerConta());
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no book-number " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlPathEqualTo(PATCH))
                .withQueryParam(COOPERATIVA_PARAM, equalTo(COOPERATIVA_VALUE))
                .withQueryParam(ORDER_ID_PARAM, equalTo(ID_PEDIDO_VALUE))
                .willReturn(aResponse().withStatus(408)));


        assertThatThrownBy(() -> acquisitionCheckingAccountClient.buscarNumeroConta(COOPERATIVA_VALUE, ID_PEDIDO_VALUE))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlPathEqualTo(PATCH))
                .withQueryParam(COOPERATIVA_PARAM, equalTo(COOPERATIVA_VALUE))
                .withQueryParam(ORDER_ID_PARAM, equalTo(ID_PEDIDO_VALUE)));

    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no book-number " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlPathEqualTo(PATCH))
                .withQueryParam(COOPERATIVA_PARAM, equalTo(COOPERATIVA_VALUE))
                .withQueryParam(ORDER_ID_PARAM, equalTo(ID_PEDIDO_VALUE))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> acquisitionCheckingAccountClient.buscarNumeroConta(COOPERATIVA_VALUE, ID_PEDIDO_VALUE))
                .isInstanceOf(RetryableException.class);


        verify(3, getRequestedFor(urlPathEqualTo(PATCH))
                .withQueryParam(COOPERATIVA_PARAM, equalTo(COOPERATIVA_VALUE))
                .withQueryParam(ORDER_ID_PARAM, equalTo(ID_PEDIDO_VALUE)));

    }
}