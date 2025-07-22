package io.sicredi.aberturadecontasalarioefetivador.client.gestest;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import feign.RetryableException;
import io.sicredi.aberturadecontasalarioefetivador.dto.GestentDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.GestentFactory;
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
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(classes = GestentConnectorApiClient.class)
@EnableFeignClients(clients = GestentConnectorApiClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
class GestentConnectorApiClientTest {

    private static final String ENTIDADE_SICREDI_URL_PATH = "/entidade-sicredi";
    @Autowired
    GestentConnectorApiClient client;
    private static final String CODIGO_TIPO_ENTIDADE = "AGENCIA";
    private static final String CODIGO_SITUACAO = "ATIVA";
    private static final String CODIGO_COOPERATIVA = "0167";
    private static final String CODIGO_AGENCIA = "17";

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve buscar entidade Sicredi")
    void deveBuscarEntidadeSicrediERetornarQuandoSucessoB() {
        stubFor(get(urlPathMatching(ENTIDADE_SICREDI_URL_PATH))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("1"))
                .withQueryParam("codigoTipoEntidade", equalTo(CODIGO_TIPO_ENTIDADE))
                .withQueryParam("codigoAgencia", equalTo(CODIGO_AGENCIA))
                .withQueryParam("codigoCooperativa", equalTo(CODIGO_COOPERATIVA))
                .withQueryParam("codigoSituacao", equalTo(CODIGO_SITUACAO))
                .willReturn(ResponseDefinitionBuilder.okForJson(GestentFactory.consultarEntidadeResponse())));

        var response = client.getEntidadeSicredi(0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO);

        assertNotNull(response);
        assertEquals(1, response.totalElements());
        assertFalse(response.content().isEmpty());
        GestentDTO.ContentRecord content = response.content().getFirst();
        assertEquals(8088, content.idEntidadeSicredi());
        assertEquals(CODIGO_TIPO_ENTIDADE, content.codigoTipoEntidade());
        assertEquals(CODIGO_COOPERATIVA, content.codigoCooperativa());
        assertEquals(CODIGO_AGENCIA, content.codigoAgencia());
    }

    @Test
    @DisplayName("Deve laçar Exception quando ocorrer erro ao buscar entidade Sicredi")
    void deveLancarExceptionQuandoOcorrerErroAoBuscarEntidadeSicrediB() {
        stubFor(get(urlPathMatching(ENTIDADE_SICREDI_URL_PATH))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("1"))
                .withQueryParam("codigoTipoEntidade", equalTo(CODIGO_TIPO_ENTIDADE))
                .withQueryParam("codigoAgencia", equalTo(CODIGO_AGENCIA))
                .withQueryParam("codigoCooperativa", equalTo(CODIGO_COOPERATIVA))
                .withQueryParam("codigoSituacao", equalTo(CODIGO_SITUACAO))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(500)));

        var exception = assertThrows(Exception.class, () -> client.getEntidadeSicredi(0, 1, CODIGO_TIPO_ENTIDADE, CODIGO_AGENCIA, CODIGO_COOPERATIVA, CODIGO_SITUACAO));

        assertInstanceOf(RetryableException.class, exception);
        verify(3, getRequestedFor(urlPathMatching(ENTIDADE_SICREDI_URL_PATH)));
    }
}