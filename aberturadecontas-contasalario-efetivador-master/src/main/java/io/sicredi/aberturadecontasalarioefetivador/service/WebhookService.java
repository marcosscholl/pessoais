package io.sicredi.aberturadecontasalarioefetivador.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Request;
import feign.Response;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.sicredi.aberturadecontasalarioefetivador.client.webhookclient.WebhookClient;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoWebhookResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Configuracao;
import io.sicredi.aberturadecontasalarioefetivador.entities.Solicitacao;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.WebhookException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@Service
@Slf4j
public class WebhookService {

    private static final String SCHEME_HTTPS = "https";
    private static final String PORTA_PADRAO_HTTPS = "443";
    private static final String PORTA_PADRAO_HTTP = "8080";
    private final int webHookTimeout;

    public WebhookService(@Value("${webhook.timeout.ms:10000}") int webHookTimeout) {
        this.webHookTimeout = webHookTimeout;
    }

    public Integer processarRetornoWebhook(Configuracao configuracao, Solicitacao solicitacaoFinalizada) {

        SolicitacaoWebhookResponseDTO resultado = SolicitacaoWebhookResponseDTO.builder()
                .idTransacao(solicitacaoFinalizada.getIdTransacao().toString())
                .status(solicitacaoFinalizada.getStatus().name())
                .resultado(solicitacaoFinalizada.getResultado().name())
                .build();

        String baseUrl = configuracao.getUrlWebhook();
        try {
            baseUrl = urlWebhookComPorta(configuracao, baseUrl);
        }  catch (WebhookException | URISyntaxException | NumberFormatException e) {
            log.error("Webhook URL ou Porta inválida na configuração: {}", e.getMessage(), e);
            return 400;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        WebhookClient webhookClient = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .requestInterceptor(template -> {
                    template.header("Content-Type", "application/json");

                    if (Objects.nonNull(configuracao.getAutorizacaoRetorno()) && !configuracao.getAutorizacaoRetorno().isBlank()) {
                        template.header("Authorization-Callback", configuracao.getAutorizacaoRetorno());
                    }
                })
                .options(new Request.Options(webHookTimeout, webHookTimeout))
                .target(WebhookClient.class, baseUrl);
        Integer statusCode = null;
        try {
            log.info("[{}] - Processando resultado ao cliente webhook", resultado.idTransacao());
            Response response = webhookClient.processarRetornoWebhook(resultado);
            statusCode = response.status();
            log.info("[{}] - Webhook response status: {}", resultado.idTransacao(), statusCode);
            if (statusCode < 200 || statusCode >= 300) {
                log.error("[{}] - Falha ao enviar resultado webhook - Código de status Http: {}", resultado.idTransacao(), statusCode);
                return statusCode;
            }
            return statusCode;
        } catch (Exception e) {
            log.error("[{}] - Falha ao enviar resultado webhook: {}", resultado.idTransacao(), e.getMessage(), e);
            return statusCode != null ? statusCode : Integer.valueOf(400);
        }
    }

    private static String urlWebhookComPorta(Configuracao configuracao, String baseUrl) throws URISyntaxException {
        URI uri = new URI(baseUrl);
        if (configuracao.getPortaHttp() != null && !configuracao.getPortaHttp().isEmpty()) {
            baseUrl = buildBaseUrl(baseUrl, configuracao.getPortaHttp());
        } else {
            String porta = SCHEME_HTTPS.equals(uri.getScheme()) ? PORTA_PADRAO_HTTPS : PORTA_PADRAO_HTTP;
            baseUrl = buildBaseUrl(baseUrl, porta);
        }
        return baseUrl;
    }

    private static String buildBaseUrl(String baseUrl, String portaHttp) {
        try {
            URI uri = new URI(baseUrl);

            URI uriComPorta = new URI(
                    uri.getScheme(),
                    uri.getUserInfo(),
                    uri.getHost(),
                    Integer.parseInt(portaHttp),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );

            baseUrl = uriComPorta.toString();
        } catch (URISyntaxException | NumberFormatException e) {
            log.error("Webhook URL ou Porta inválida na configuração: {}", e.getMessage(), e);
            throw new WebhookException("Webhook URL ou Porta inválida na configuração", e);
        }
        return baseUrl;
    }

    public boolean webhookConectividade(Configuracao configuracao) {
        String baseUrl = configuracao.getUrlWebhook();
        try {
            baseUrl = urlWebhookComPorta(configuracao, baseUrl);

            WebhookClient webhookClient = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(webHookTimeout, webHookTimeout))
                .target(WebhookClient.class, baseUrl);

            Response response = webhookClient.ping();
            log.info("Resultado de consulta da conectivade ao Webhook : Url[{}] HttpStatus[{}]", baseUrl, response.status());
            return ((response.status() >= 200 && response.status() < 300) || response.status() == 401);
        } catch (Exception e) {
            log.error("Erro ao consultar conectivade ao Webhook [{}] : {}", baseUrl, e.getMessage());
            return false;
        }
    }
}
