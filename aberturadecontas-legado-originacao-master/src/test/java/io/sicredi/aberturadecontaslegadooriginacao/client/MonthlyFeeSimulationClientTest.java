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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = MonthlyFeeSimulationClient.class)
@EnableFeignClients(clients = MonthlyFeeSimulationClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class MonthlyFeeSimulationClientTest {

    @Autowired
    private MonthlyFeeSimulationClient monthlyFeeSimulationClient;
    private static final String ID_SIMULACAO = "67e2e1d992f99d79e007ceb1";
    private static final String MONTHLY_FEE_SIMULATION_URL_PATH = "/simulations/" + ID_SIMULACAO;

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve retornar dados do pedido ao realizar consulta no acquisition-orders")
    void deveRetornarOsDadosDoPedidoAoRealizarConsultaNoAcquisitionOrders() {

        stubFor(get(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("accountLegacy/monthly-fee-simulation/dados-cesta-relacionamento.json")));

        var dadosCestaRelacionamento = monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(ID_SIMULACAO);

        List<ServeEvent> serveEvents = getAllServeEvents();
        ServeEvent serveEvent = serveEvents.getFirst();
        String responseBody = serveEvent.getResponse().getBodyAsString();

        assertThat(dadosCestaRelacionamento.diaPagamento()).isEqualTo(JsonPath.read(responseBody, "$.paymentDay"));
        assertThat(dadosCestaRelacionamento.id()).isEqualTo(JsonPath.read(responseBody, "$.coreId"));

        verify(1, getRequestedFor(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao realizar consulta no monthly-fee-simulation " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        stubFor(get(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH))
                .willReturn(aResponse().withStatus(408)));

        assertThatThrownBy(() -> monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(ID_SIMULACAO))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao realizar consulta no monthly-fee-simulation " +
            "e lançar RetryableException")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        stubFor(get(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> monthlyFeeSimulationClient.buscarDadosCestaRelacionamento(ID_SIMULACAO))
                .isInstanceOf(RetryableException.class);

        verify(3, getRequestedFor(urlMatching(MONTHLY_FEE_SIMULATION_URL_PATH)));
    }
}