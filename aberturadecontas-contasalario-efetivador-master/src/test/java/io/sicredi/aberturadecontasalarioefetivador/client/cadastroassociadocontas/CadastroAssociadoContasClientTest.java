package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadocontas;

import feign.*;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import io.sicredi.aberturadecontasalarioefetivador.config.FeignClientConfiguration;
import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;
import io.sicredi.aberturadecontasalarioefetivador.factories.CadastroAssociadoContasFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CadastroAssociadoContasClientTest {

    private static final String DOCUMENTO = "20643481400";
    private static final String COOPERATIVA = "0167";
    private static final String STATUS_CONTA = "ATIVA";
    private static final String TIPO_CONTA = "CONTA_SALARIO";
    private static final String TIPO_RELACIONAMENTO = "TITULAR";
    private static final String STRING_ERRO = "Erro Interno do Servidor";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String HTTP_STATUS_OK = "OK";
    private static final String NUM_CONTA = "903677";
    private static final int PERIOD = 100;
    private static final int MAX_PERIOD = 200;
    private static final int MAX_ATTEMPTS = 3;

    interface TestCadastroAssociadoContasClient {
        @RequestLine("GET /v3/associado-contas/associados/{documento}/contas?cooperativa={cooperativa}&statusConta={statusConta}&tipoConta={tipoConta}&tipoRelacionamento={tipoRelacionamento}")
        List<CadastroAssociadoContasDTO> getContas(
                @Param("documento") String documento,
                @Param("cooperativa") String cooperativa,
                @Param("statusConta") String statusConta,
                @Param("tipoConta") String tipoConta,
                @Param("tipoRelacionamento") String tipoRelacionamento
        );
    }

    interface TestCadastroAssociadoContasSalarioClient {
        @RequestLine("GET /v3/associado-contas/associados/{documento}/contas?statusConta={statusConta}&tipoConta={tipoConta}&tipoRelacionamento={tipoRelacionamento}")
        List<CadastroAssociadoContasDTO> getContas(
                @Param("documento") String documento,
                @Param("statusConta") String statusConta,
                @Param("tipoConta") String tipoConta,
                @Param("tipoRelacionamento") String tipoRelacionamento
        );
    }

    interface TestCadastroAssociadoContasDigitalClient {
        @RequestLine("GET /v3/associado-contas/associados/{documento}/contas?statusConta={statusConta}")
        List<CadastroAssociadoContasDTO> buscarContasAtivas(
                @Param("documento") String documento,
                @Param("statusConta") String statusConta
        );
    }

    @Test
    @DisplayName("Deve retornar lista de AssociadoConta")
    void deveRetornarListaDeAssociadoConta() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJson(), StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertNotNull(retornado);
        assertEquals(1, retornado.size());
        assertEquals(COOPERATIVA, retornado.getFirst().cooperativa());
        assertEquals(NUM_CONTA, retornado.getFirst().conta());
    }

    @Test
    @DisplayName("Deve retornar Exception quando ocorrer erro")
    void deveRetornarExceptionQuandoErro() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(500)
                        .reason(INTERNAL_SERVER_ERROR)
                        .headers(Collections.emptyMap())
                        .body(STRING_ERRO, StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var exception = assertThrows(Exception.class, () ->
                client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO));

        assertTrue(exception.getMessage().contains(STRING_ERRO));
    }

    @Test
    @DisplayName("Deve efetuar retry quando ocorrere erro HTTP 500")
    void deveEfetuarRetryQuandoErroHTTP500() {
        var countRetry = new AtomicInteger(0);

        Client failingClient = (request, options) -> {
            countRetry.incrementAndGet();
            return Response.builder()
                    .status(500)
                    .reason(INTERNAL_SERVER_ERROR)
                    .headers(Collections.emptyMap())
                    .body(STRING_ERRO, StandardCharsets.UTF_8)
                    .request(request)
                    .build();
        };

        var retryer = new Retryer.Default(PERIOD, MAX_PERIOD, MAX_ATTEMPTS);
        var errorDecoder = new FeignClientConfiguration(PERIOD, MAX_PERIOD, MAX_ATTEMPTS).errorDecoder();

        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(errorDecoder)
                .retryer(retryer)
                .client(failingClient)
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var exception = assertThrows(Exception.class, () -> client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO));

        assertInstanceOf(RetryableException.class, exception);
        assertEquals(MAX_ATTEMPTS, countRetry.get());
    }

    @Test
    @DisplayName("Deve retornar sucesso no terceiro retry")
    void deveRetornarSucessoNoTerceiroRetry() {
        var countRetry = new AtomicInteger(0);

        Client customClient = (request, options) -> {
            int attempt = countRetry.incrementAndGet();
            if (attempt < MAX_ATTEMPTS) {
                // Falha nas duas primeiras tentativas
                return Response.builder()
                        .status(500)
                        .reason(INTERNAL_SERVER_ERROR)
                        .headers(Collections.emptyMap())
                        .body(STRING_ERRO, StandardCharsets.UTF_8)
                        .request(request)
                        .build();
            } else {
                // Sucesso na terceira tentativa
                return Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJson(), StandardCharsets.UTF_8)
                        .request(request)
                        .build();
            }
        };

        var errorDecoder = new FeignClientConfiguration(PERIOD, MAX_PERIOD, MAX_ATTEMPTS).errorDecoder();

        var retryer = new Retryer.Default(PERIOD, MAX_PERIOD, MAX_ATTEMPTS);

        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(errorDecoder)
                .retryer(retryer)
                .client(customClient)
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertNotNull(retornado);
        assertEquals(1, retornado.size());
        assertEquals(COOPERATIVA, retornado.getFirst().cooperativa());
        assertEquals(NUM_CONTA, retornado.getFirst().conta());
        assertEquals(MAX_ATTEMPTS, countRetry.get());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nenhuma conta for encontrada")
    void deveRetornarListaVaziaQuandoNenhumaContaEncontrada() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body("[]", StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertThat(retornado).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Deve ignorar propriedades desconhecidas")
    void deveIgnorarPropriedadesDesconhecidas() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJsonComPropriedadesDesconhecidas(), StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertThat(retornado).isNotNull().hasSize(1);
        assertThat(retornado.getFirst().cooperativa()).isEqualTo(COOPERATIVA);
        assertThat(retornado.getFirst().conta()).isEqualTo(NUM_CONTA);
    }

    @Test
    @DisplayName("Deve aceitar valores nulos")
    void deveAceitarValoresNulos() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJsonComValoresNulos(), StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, COOPERATIVA, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertThat(retornado).isNotNull().hasSize(1);
        assertThat(retornado.getFirst().cooperativa()).isNull();
        assertThat(retornado.getFirst().dataAbertura()).isNull();
    }

    @Test
    @DisplayName("Deve consultar contas ativas do associado e retorná-las")
    void deveConsultarContasAtivasAssociadoERetornarQuandoEncontrar() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJson(), StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasDigitalClient.class, HTTP_LOCALHOST);

        var retornado = client.buscarContasAtivas(DOCUMENTO, STATUS_CONTA);

        assertNotNull(retornado);
        assertEquals(1, retornado.size());
        assertEquals(COOPERATIVA, retornado.getFirst().cooperativa());
        assertEquals(NUM_CONTA, retornado.getFirst().conta());
    }

    @Test
    @DisplayName("Deve retornar lista de AssociadoConta para ContaSalario")
    void deveRetornarListaDeAssociadoContaSalario() {
        var client = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new ErrorDecoder.Default())
                .client((request, options) -> Response.builder()
                        .status(MAX_PERIOD)
                        .reason(HTTP_STATUS_OK)
                        .headers(Collections.emptyMap())
                        .body(CadastroAssociadoContasFactory.criaCadastroAssociadoContasJson(), StandardCharsets.UTF_8)
                        .request(request)
                        .build())
                .target(TestCadastroAssociadoContasSalarioClient.class, HTTP_LOCALHOST);

        var retornado = client.getContas(DOCUMENTO, STATUS_CONTA, TIPO_CONTA, TIPO_RELACIONAMENTO);

        assertNotNull(retornado);
        assertEquals(1, retornado.size());
        assertEquals(COOPERATIVA, retornado.getFirst().cooperativa());
        assertEquals(NUM_CONTA, retornado.getFirst().conta());
    }
}