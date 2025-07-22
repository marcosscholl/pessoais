package io.sicredi.aberturadecontasalarioefetivador.config;

import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.RetryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;


class FeignRetryErrorDecoderTest {


    @Test
    @DisplayName("Deve retornar RetryableException quando código de erro for 408")
    void deveRetornarRetryableExceptionQuandoCodigoDeErroFor408(){
        FeignRetryErrorDecoder feignRetryErrorDecoder = new FeignRetryErrorDecoder();
        Request request = Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), new byte[10],
                Charset.defaultCharset());
        Response response = Response.builder().request(request).status(408).build();
        Exception exception = feignRetryErrorDecoder.decode("GET", response);

        assertInstanceOf(RetryableException.class, exception);
    }

    @Test
    @DisplayName("Deve retornar RetryableException quando código de erro for 5XX")
    void deveRetornarRetryableExceptionQuandoCodigoDeErroFor5XX(){
        FeignRetryErrorDecoder feignRetryErrorDecoder = new FeignRetryErrorDecoder();
        Request request = Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), new byte[10],
                Charset.defaultCharset());
        Response response1 = Response.builder().request(request).status(500).build();
        Response response2 = Response.builder().request(request).status(599).build();
        Exception responseException1 = feignRetryErrorDecoder.decode("GET", response1);
        Exception responseException2 = feignRetryErrorDecoder.decode("GET", response2);

        assertInstanceOf(RetryableException.class, responseException1);
        assertInstanceOf(RetryableException.class, responseException2);
    }

    @Test
    @DisplayName("Deve retornar a exception própria do error code ")
    void deveRetornarAPopriaExceptionDoErrorCode(){
        FeignRetryErrorDecoder feignRetryErrorDecoder = new FeignRetryErrorDecoder();
        Request request = Request.create(Request.HttpMethod.GET, "",
                new HashMap<>(), new byte[10],
                Charset.defaultCharset());
        Response response1 = Response.builder().request(request).status(400).build();
        Response response2 = Response.builder().request(request).status(600).build();
        Exception responseException1 = feignRetryErrorDecoder.decode("GET", response1);
        Exception responseException2 = feignRetryErrorDecoder.decode("GET", response2);

        assertInstanceOf(FeignException.BadRequest.class, responseException1);
        assertInstanceOf(FeignException.class, responseException2);
    }
}