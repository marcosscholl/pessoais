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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = AcquisitionOrdersClient.class)
@EnableFeignClients(clients = AcquisitionOrdersClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class AcquisitionOrdersClientTest {

    @Autowired
    private AcquisitionOrdersClient acquisitionOrdersClient;
    private static final String ORDER_ID = "67e2e1d992f99d79e007ceb1";
    private static final String ORDERS_URL_PATH = "/orders/" + ORDER_ID;

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar dados do pedido ao realizar consulta no acquisition-orders")
    void deveRetornarOsDadosDoPedidoAoRealizarConsultaNoAcquisitionOrders() {

        stubFor(get(urlMatching(ORDERS_URL_PATH))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("acquisitionOrders/pedidoCompleto.json")));

        var acquisitionOrdersDTO = acquisitionOrdersClient.buscaPedido(ORDER_ID);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String pedidoEsperado = serveEvent.getResponse().getBodyAsString();

        assertThat(acquisitionOrdersDTO.id()).isEqualTo(JsonPath.read(pedidoEsperado, "$.id"));
        assertThat(acquisitionOrdersDTO.idOferta()).isEqualTo(JsonPath.read(pedidoEsperado, "$.offerId"));
        assertThat(acquisitionOrdersDTO.cooperativa()).isEqualTo(JsonPath.read(pedidoEsperado, "$.organization"));
        assertThat(acquisitionOrdersDTO.agencia()).isEqualTo(JsonPath.read(pedidoEsperado, "$.agency"));
        assertThat(acquisitionOrdersDTO.status().name()).isEqualTo(JsonPath.read(pedidoEsperado, "$.status"));
        assertThat(acquisitionOrdersDTO.trackerStatus()).isEqualTo(JsonPath.read(pedidoEsperado, "$.trackerStatus"));
        assertThat(acquisitionOrdersDTO.criadoPor()).isEqualTo(JsonPath.read(pedidoEsperado, "$.createdBy"));
        assertThat(acquisitionOrdersDTO.canal()).isEqualTo(JsonPath.read(pedidoEsperado, "$.originSource"));
        assertThat(acquisitionOrdersDTO.tipoInscricao()).isEqualTo(JsonPath.read(pedidoEsperado, "$.enrollmentType"));
        assertThat(acquisitionOrdersDTO.dataInicio()).isEqualTo(dataHoraSemFuso(JsonPath.read(pedidoEsperado, "$.startDate")));
        assertThat(acquisitionOrdersDTO.produtos()).allSatisfy(produtoDTO -> {
            var indiceProduto = acquisitionOrdersDTO.produtos().indexOf(produtoDTO);
            var jsonPathProduto = "$.items.[" + indiceProduto + "]";
            assertThat(produtoDTO.id()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".id"));
            assertThat(produtoDTO.idSimulacao()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".simulationId"));
            assertThat(produtoDTO.idCatalogoProduto()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".productCatalogId"));
            assertThat(produtoDTO.tipoProduto()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".productType"));
            assertThat(produtoDTO.codigoProduto()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".productCode"));
            assertThat(produtoDTO.marca()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".brand"));
            assertThat(produtoDTO.status().name()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + ".status"));
            assertThat(produtoDTO.cadastros()).allSatisfy(cadastroDTO -> {
                var indiceCadastro = produtoDTO.cadastros().indexOf(cadastroDTO);
                var jsonPathCadastro = ".customers.[" + indiceCadastro + "]";
                assertThat(cadastroDTO.idCadastro()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + jsonPathCadastro + ".customerId"));
                assertThat(cadastroDTO.cpf()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + jsonPathCadastro + ".suid"));
                assertThat(cadastroDTO.semPermissao()).isEqualTo(JsonPath.read(pedidoEsperado, jsonPathProduto + jsonPathCadastro + ".permissionLess"));
            });
        });

        verify(1, getRequestedFor(urlMatching(ORDERS_URL_PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no acquisition-order " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlMatching(ORDERS_URL_PATH))
                .willReturn(aResponse().withStatus(408)));

        assertThatThrownBy(() -> acquisitionOrdersClient.buscaPedido(ORDER_ID))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(ORDERS_URL_PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no acquisition-order " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlMatching(ORDERS_URL_PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> acquisitionOrdersClient.buscaPedido(ORDER_ID))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(ORDERS_URL_PATH)));
    }
}