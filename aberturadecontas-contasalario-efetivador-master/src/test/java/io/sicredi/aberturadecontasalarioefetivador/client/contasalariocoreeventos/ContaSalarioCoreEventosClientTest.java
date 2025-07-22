package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocoreeventos;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import feign.RetryableException;
import io.sicredi.aberturadecontasalarioefetivador.factories.ContaSalarioCoreEventosFactory;
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
@SpringBootTest(classes = ContaSalarioCoreEventosClient.class)
@EnableFeignClients(clients = ContaSalarioCoreEventosClient.class)
@AutoConfigureWireMock(port = 9999)
@EnableAutoConfiguration
class ContaSalarioCoreEventosClientTest {

    private static final String CONTA_SALARIO_CORE_EVENTOS_URL_PATH = "/eventos/agencia/0167/conta/903677";
    public static final String AGENCIA = "0167";
    public static final String CONTA = "903677";
    public static final String QUERY_PARAM_TIPOS = "tipos";


    @Autowired
    ContaSalarioCoreEventosClient client;

    @BeforeEach
    void resetWiremock(){
        resetAllRequests();
    }

    @Test
    @DisplayName("Deve buscar eventos conta salario e retornar quando encontrar")
    void deveBuscarEventosContaSalarioERetornarQuandoEncontrar() {
        stubFor(get(urlPathMatching(CONTA_SALARIO_CORE_EVENTOS_URL_PATH))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ALTERACAO_PORTABILIDADE"))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ALTERACAO_CONVENIO"))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ENCERRAMENTO_CONTA_SALARIO"))
                .willReturn(ResponseDefinitionBuilder.okForJson(ContaSalarioCoreEventosFactory.contaSalarioCoreEventos())));

        var response = client.buscarEventosContaSalario(AGENCIA, CONTA);

        assertNotNull(response);
        assertEquals(7, response.size());
        assertFalse(response.isEmpty());
        assertEquals(AGENCIA, response.getFirst().agencia());
        assertEquals(CONTA, response.getFirst().conta());
    }

    @Test
    @DisplayName("Deve laçar Exception quando ocorrer erro ao buscar eventos conta salário")
    void deveLancarExceptionQuandoOcorrerErroAoBuscarEventosContaSalario() {
        stubFor(get(urlPathMatching(CONTA_SALARIO_CORE_EVENTOS_URL_PATH))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ALTERACAO_PORTABILIDADE"))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ALTERACAO_CONVENIO"))
                .withQueryParam(QUERY_PARAM_TIPOS, equalTo("ENCERRAMENTO_CONTA_SALARIO"))
                .willReturn(ResponseDefinitionBuilder.responseDefinition().withStatus(500)));

        var exception = assertThrows(Exception.class, () -> client.buscarEventosContaSalario(AGENCIA, CONTA));

        assertInstanceOf(RetryableException.class, exception);
        verify(3, getRequestedFor(urlPathMatching(CONTA_SALARIO_CORE_EVENTOS_URL_PATH)));
    }
}