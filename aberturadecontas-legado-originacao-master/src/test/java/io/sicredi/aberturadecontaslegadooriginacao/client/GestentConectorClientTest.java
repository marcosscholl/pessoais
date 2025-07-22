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
@SpringBootTest(classes = GestentConectorClient.class)
@EnableFeignClients(clients = GestentConectorClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class GestentConectorClientTest {

    @Autowired
    private GestentConectorClient gestentConectorClient;

    private static final String PATCH ="/entidade-sicredi-completo";
    private static final String PAGE_VALUE = "0";
    private static final String PAGE_PARAM = "page";
    private static final String PAGE_SIZE_VALUE = "1";
    private static final String PAGE_SIZE_PARAM = "pageSize";
    private static final String CODIGO_TIPO_ENTIDADE_VALUE = "AGENCIA";
    private static final String CODIGO_TIPO_ENTIDADE_PARAM = "codigoTipoEntidade";
    private static final String CODIGO_AGENCIA_VALUE = "17";
    private static final String CODIGO_AGENCIA_PARAM = "codigoAgencia";
    private static final String CODIGO_COOPERATIVA_VALUE = "0167";
    private static final String CODIGO_COOPERATIVA_PARAM = "codigoCooperativa";
    private static final String CODIGO_SITUACAO_PARAM = "codigoSituacao";


    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar o código da entidade no gestent-conector")
    void deveRetornarCodigoDaEntidadeAoRealizarConsultaNoGestentConector() {
        stubFor(get(urlPathEqualTo(PATCH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("gestent.json")));

        var codigoEntidade = gestentConectorClient.buscarCodigoEntidade(CODIGO_COOPERATIVA_VALUE,CODIGO_AGENCIA_VALUE);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String responseBody = serveEvent.getResponse().getBodyAsString();

        verify(1, getRequestedFor(urlPathEqualTo(PATCH)));
        assertEquals(JsonPath.read(responseBody, "$.content[0].codigoEntidade"), codigoEntidade.codigoEntidade().getFirst().codigoEntidade());
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no gestent-conector " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlPathEqualTo(PATCH))
                .withQueryParam(PAGE_PARAM, equalTo(PAGE_VALUE))
                .withQueryParam(PAGE_SIZE_PARAM, equalTo(PAGE_SIZE_VALUE))
                .withQueryParam(CODIGO_TIPO_ENTIDADE_PARAM, equalTo(CODIGO_TIPO_ENTIDADE_VALUE))
                .withQueryParam(CODIGO_AGENCIA_PARAM, equalTo(CODIGO_AGENCIA_VALUE))
                .withQueryParam(CODIGO_COOPERATIVA_PARAM, equalTo(CODIGO_COOPERATIVA_VALUE))
                .willReturn(aResponse().withStatus(408)));


        assertThatThrownBy(() -> gestentConectorClient.buscarCodigoEntidade(CODIGO_COOPERATIVA_VALUE,CODIGO_AGENCIA_VALUE))
                .isInstanceOf(RetryableException.class);


        verify(3, getRequestedFor(urlPathEqualTo(PATCH))
                .withQueryParam(PAGE_PARAM, equalTo(PAGE_VALUE))
                .withQueryParam(PAGE_SIZE_PARAM, equalTo(PAGE_SIZE_VALUE))
                .withQueryParam(CODIGO_TIPO_ENTIDADE_PARAM, equalTo(CODIGO_TIPO_ENTIDADE_VALUE))
                .withQueryParam(CODIGO_AGENCIA_PARAM, equalTo(CODIGO_AGENCIA_VALUE))
                .withQueryParam(CODIGO_COOPERATIVA_PARAM, equalTo(CODIGO_COOPERATIVA_VALUE)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no gestent-conector " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlPathEqualTo(PATCH))
                .withQueryParam(PAGE_PARAM, equalTo(PAGE_VALUE))
                .withQueryParam(PAGE_SIZE_PARAM, equalTo(PAGE_SIZE_VALUE))
                .withQueryParam(CODIGO_TIPO_ENTIDADE_PARAM, equalTo(CODIGO_TIPO_ENTIDADE_VALUE))
                .withQueryParam(CODIGO_AGENCIA_PARAM, equalTo(CODIGO_AGENCIA_VALUE))
                .withQueryParam(CODIGO_COOPERATIVA_PARAM, equalTo(CODIGO_COOPERATIVA_VALUE))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> gestentConectorClient.buscarCodigoEntidade(CODIGO_COOPERATIVA_VALUE,CODIGO_AGENCIA_VALUE))
                .isInstanceOf(RetryableException.class);


        verify(3, getRequestedFor(urlPathEqualTo(PATCH))
                .withQueryParam(PAGE_PARAM, equalTo(PAGE_VALUE))
                .withQueryParam(PAGE_SIZE_PARAM, equalTo(PAGE_SIZE_VALUE))
                .withQueryParam(CODIGO_TIPO_ENTIDADE_PARAM, equalTo(CODIGO_TIPO_ENTIDADE_VALUE))
                .withQueryParam(CODIGO_AGENCIA_PARAM, equalTo(CODIGO_AGENCIA_VALUE))
                .withQueryParam(CODIGO_COOPERATIVA_PARAM, equalTo(CODIGO_COOPERATIVA_VALUE)));

    }
}