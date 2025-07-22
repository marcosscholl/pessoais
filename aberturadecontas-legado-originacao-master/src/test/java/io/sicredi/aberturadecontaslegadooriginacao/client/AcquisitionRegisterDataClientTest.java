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
import static io.sicredi.aberturadecontaslegadooriginacao.utils.TestUtils.dataHoraSemFuso;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(classes = AcquisitionRegisterDataClient.class)
@EnableFeignClients(clients = AcquisitionRegisterDataClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class AcquisitionRegisterDataClientTest {

    @Autowired
    private AcquisitionRegisterDataClient acquisitionRegisterDataClient;

    private static final String ID_PEDIDO = "654bc13c1078fa3f8fd19487";
    private static final String URL = "/orders/" + ID_PEDIDO + "/documents";

    @BeforeEach
    void resetWiremock() {
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar os documentos ao realizar consulta no acquisition-register-data")
    void deveRetornarOsDocumentosDoPedidoAoRealizarConsultaNoRegisterData() {
        stubFor(get(urlMatching(URL))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("acquisitionRegisterData/documentos.json")));

        var documentosDoPedido = acquisitionRegisterDataClient.buscarDocumentosDoPedido(ID_PEDIDO);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String responseBody = serveEvent.getResponse().getBodyAsString();

        verify(1, getRequestedFor(urlMatching(URL)));
        assertEquals(JsonPath.read(responseBody, "$[0].id"), documentosDoPedido.getFirst().id());
        assertEquals(JsonPath.read(responseBody, "$[0].key"), documentosDoPedido.getFirst().chave());
        assertEquals(JsonPath.read(responseBody, "$[0].orderId"), documentosDoPedido.getFirst().idPedido());
        assertEquals(JsonPath.read(responseBody, "$[0].customerId"), documentosDoPedido.getFirst().idCadastro());
        assertEquals(JsonPath.read(responseBody, "$[0].type"), documentosDoPedido.getFirst().tipo());
        assertEquals(JsonPath.read(responseBody, "$[0].migratedToCustomer"), documentosDoPedido.getFirst().migradoParaCadastro());
        assertEquals(JsonPath.read(responseBody, "$[0].version"), documentosDoPedido.getFirst().versao());
        assertEquals(JsonPath.read(responseBody, "$[0].status"), documentosDoPedido.getFirst().status().name());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$[0].createdDate")), documentosDoPedido.getFirst().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$[0].source"), documentosDoPedido.getFirst().canal());

        assertEquals(JsonPath.read(responseBody, "$[1].id"), documentosDoPedido.getLast().id());
        assertEquals(JsonPath.read(responseBody, "$[1].key"), documentosDoPedido.getLast().chave());
        assertEquals(JsonPath.read(responseBody, "$[1].orderId"), documentosDoPedido.getLast().idPedido());
        assertEquals(JsonPath.read(responseBody, "$[1].customerId"), documentosDoPedido.getLast().idCadastro());
        assertEquals(JsonPath.read(responseBody, "$[1].type"), documentosDoPedido.getLast().tipo());
        assertEquals(JsonPath.read(responseBody, "$[1].migratedToCustomer"), documentosDoPedido.getLast().migradoParaCadastro());
        assertEquals(JsonPath.read(responseBody, "$[1].version"), documentosDoPedido.getLast().versao());
        assertEquals(JsonPath.read(responseBody, "$[1].status"), documentosDoPedido.getLast().status().name());
        assertEquals(dataHoraSemFuso(JsonPath.read(responseBody, "$[1].createdDate")), documentosDoPedido.getLast().dataCriacao());
        assertEquals(JsonPath.read(responseBody, "$[1].source"), documentosDoPedido.getLast().canal());
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no acquisition-register-data " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlMatching(URL))
                .willReturn(aResponse().withStatus(408)));

        assertThatThrownBy(() -> acquisitionRegisterDataClient.buscarDocumentosDoPedido(ID_PEDIDO))
                .isInstanceOf(RetryableException.class);


        verify(3, getRequestedFor(urlMatching(URL)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no acquisition-register-data " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlMatching(URL))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> acquisitionRegisterDataClient.buscarDocumentosDoPedido(ID_PEDIDO))
                .isInstanceOf(RetryableException.class);


        verify(3, getRequestedFor(urlMatching(URL)));
    }

}