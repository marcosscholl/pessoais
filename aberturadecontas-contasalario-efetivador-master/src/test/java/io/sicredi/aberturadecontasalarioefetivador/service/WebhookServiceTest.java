package io.sicredi.aberturadecontasalarioefetivador.service;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoWebhookResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Resultado;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Status;
import io.sicredi.aberturadecontasalarioefetivador.factories.ConfiguracaoFactory;
import io.sicredi.aberturadecontasalarioefetivador.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {
    private WebhookService webhookService;
    private static final BigInteger ID_TRANSACAO = new BigInteger("202409301181156156165196151");
    private static final Solicitacao SOLICITACAO_FINALIZADA = Solicitacao.builder()
            .status(Status.FINALIZADO)
            .resultado(Resultado.CONCLUIDO)
            .idTransacao(ID_TRANSACAO)
            .build();

    private static final SolicitacaoWebhookResponseDTO RESULTADO_WEBHOOK = SolicitacaoWebhookResponseDTO.builder()
            .status(Status.FINALIZADO.name())
            .resultado(Resultado.CONCLUIDO.name())
            .idTransacao(ID_TRANSACAO.toString())
            .build();
    private static final String BASE_PATH_WEBHOOK = "/webhook";
    public static final String URL = "/";




    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().build();

    @BeforeEach
    void setUp() {
        int webHookTimeout = 5000;
        webhookService = new WebhookService(webHookTimeout);
    }

    @Test
    void deveriaProcessarRetornoWebhook_sucessoComPorta() {
        mockEVerifySucesso(ConfiguracaoFactory.configuracaoValida(String.valueOf(wireMockServer.getPort())));
    }

    @Test
    void deveriaProcessarRetornoWebhook_sucessoComAutorizacaoRetorno() {
        mockEVerifySucesso(ConfiguracaoFactory.configuracaoComAutorizacaoRetorno(String.valueOf(wireMockServer.getPort())));
    }

    @Test
    void deveriaProcessarRetornoWebhook_erroNoEnvio() {
        wireMockServer.stubFor(post(urlEqualTo(BASE_PATH_WEBHOOK))
                .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK)))
                .willReturn(aResponse()
                        .withStatus(500)));

        mockEVerifyFalha(ConfiguracaoFactory.configuracaoValida(String.valueOf(wireMockServer.getPort())), 500);
    }

    @Test
    void deveriaProcessarRetornoWebhook_statusCodeNaoSucesso() {
        wireMockServer.stubFor(post(urlEqualTo(BASE_PATH_WEBHOOK))
                .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK)))
                .willReturn(aResponse()
                        .withStatus(400)));

        mockEVerifyFalha(ConfiguracaoFactory.configuracaoValida(String.valueOf(wireMockServer.getPort())), 400);

        wireMockServer.verify(postRequestedFor(urlEqualTo(BASE_PATH_WEBHOOK))
                .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK))));
    }

    @Test
    void deveriaProcessarRetornoWebhook_urlInvalida() {
        mockEVerifyFalha(ConfiguracaoFactory.configuracaoUrlInvalida(String.valueOf(wireMockServer.getPort())), 400);
    }

    @Test
    void deveriarocessarRetornoWebhook_portaInvalida() {
        mockEVerifyFalha(ConfiguracaoFactory.configuracaoPortaInvalida(), 400);
    }

    private void mockEVerifySucesso(Configuracao configuracao) {

        if (Objects.isNull(configuracao.getAutorizacaoRetorno())) {
            wireMockServer.stubFor(post(urlEqualTo(BASE_PATH_WEBHOOK))
                    .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK)))
                    .willReturn(aResponse()
                            .withStatus(202)));
        } else {
            wireMockServer.stubFor(post(urlEqualTo(BASE_PATH_WEBHOOK))
                    .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK)))
                            .withHeader("Authorization-Callback", matching("sWa1LnR4XqHrDDQp5WaRGrKXYDcyLBvk"))
                    .willReturn(aResponse()
                            .withStatus(202)));
        }

        int retornoWebhook = webhookService.processarRetornoWebhook(configuracao, SOLICITACAO_FINALIZADA);
        assertEquals(202, retornoWebhook);
        wireMockServer.verify(postRequestedFor(urlEqualTo(BASE_PATH_WEBHOOK))
                .withRequestBody(equalToJson(TestUtils.objetoString(RESULTADO_WEBHOOK))));
    }

    private void mockEVerifyFalha(Configuracao configuracao, int httpStatus) {
        int retornoWebhook = webhookService.processarRetornoWebhook(configuracao, SOLICITACAO_FINALIZADA);
        assertEquals(httpStatus, retornoWebhook);
    }

    @Test
    void testarConectividadeWebhookSucesso2xxRetornatrue() {
        wireMockServer.stubFor(options(urlPathEqualTo(URL))
                .willReturn(aResponse().withStatus(200)));

        boolean conectado = webhookService.webhookConectividade(getConfiguracao());
        assertThat(conectado).isTrue();
    }

    private static Configuracao getConfiguracao() {
        return Configuracao.builder().urlWebhook(wireMockServer.getRuntimeInfo().getHttpBaseUrl())
                .portaHttp(String.valueOf(wireMockServer.getRuntimeInfo().getHttpPort()))
                .build();
    }

    @Test
    void testarConectividadeWebhookErro4xxRetornafalse() {
        wireMockServer.stubFor(options(urlPathEqualTo(URL))
                .willReturn(aResponse().withStatus(404)));

        boolean conectado = webhookService.webhookConectividade(getConfiguracao());
        assertThat(conectado).isFalse();
    }

    @Test
    void testarConectividadeWebhookErro401RetornarTrue() {
        wireMockServer.stubFor(options(urlPathEqualTo(URL))
                .willReturn(aResponse().withStatus(401)));

        boolean conectado = webhookService.webhookConectividade(getConfiguracao());
        assertThat(conectado).isTrue();
    }

    @Test
    void testarConectividadeWebhookTimeoutRetornafalse() {
        wireMockServer.stubFor(options(urlPathEqualTo(URL))
                .willReturn(aResponse().withFixedDelay(6000)));

        boolean conectado = webhookService.webhookConectividade(getConfiguracao());
        assertThat(conectado).isFalse();
    }

    @Test
    void testarConectividadeWebhookConexaoInvalidaRetornafalse() {
        boolean conectado = webhookService.webhookConectividade(Configuracao.builder().urlWebhook("http://localhost:9999").build());
        assertThat(conectado).isFalse();
    }

}