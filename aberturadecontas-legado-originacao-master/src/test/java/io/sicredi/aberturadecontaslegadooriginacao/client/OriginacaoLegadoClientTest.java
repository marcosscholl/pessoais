package io.sicredi.aberturadecontaslegadooriginacao.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;
import feign.RetryableException;
import io.sicredi.aberturadecontaslegadooriginacao.config.DisableDataSourceConfig;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.*;
import org.instancio.Instancio;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.sicredi.aberturadecontaslegadooriginacao.utils.TestUtils.dataHoraSemFuso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest(classes = OriginacaoLegadoClient.class)
@EnableFeignClients(clients = OriginacaoLegadoClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
@Import(DisableDataSourceConfig.class)
class OriginacaoLegadoClientTest {

    @Autowired
    OriginacaoLegadoClient originacaoLegadoClient;

    private static final String PATH = "/processar";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetWiremock() {
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve realizar efetivacao de Originação Fisital Legado No BPEL e receber resposta com dados correspondentes")
    void deveRealizarEfetivacaoDeOriginacaoFisitalLegadoNoBPELEReceberResposta() {
        var originacaoLegadoDTO = Instancio.create(PedidoDTO.class);
        String jsonPath = "originacaoLegado/originacaoLegado.json";

        stubFor(post(urlPathEqualTo(PATH))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile(jsonPath)));

        originacaoLegadoClient.processaOriginacao(originacaoLegadoDTO);

        verify(1, postRequestedFor(urlPathEqualTo(PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 408 ao tentar realizar efetivação de cadastros/contas no BPEL")
    void deveRealizar3TentativasAoReceberHTTP408ELancarRetryableException() {
        var originacaoLegadoDTO = Instancio.create(PedidoDTO.class);
        stubFor(post(urlPathEqualTo(PATH))
                .willReturn(aResponse().withStatus(408)));

        assertThatThrownBy(() -> originacaoLegadoClient.processaOriginacao(originacaoLegadoDTO))
                .isInstanceOf(RetryableException.class);


        verify(3, postRequestedFor(urlPathEqualTo(PATH)));
    }

    @Test
    @DisplayName("Deve realizar 3 tentativas ao receber um HTTP 5XX ao tentar realizar efetivação de cadastros/contas no BPEL")
    void deveRealizar3TentativasAoReceberHTTP5XXELancarRetryableException() {
        var originacaoLegadoDTO = Instancio.create(PedidoDTO.class);
        stubFor(post(urlPathEqualTo(PATH))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> originacaoLegadoClient.processaOriginacao(originacaoLegadoDTO))
                .isInstanceOf(RetryableException.class);
        verify(3, postRequestedFor(urlPathEqualTo(PATH)));

    }

}