package io.sicredi.aberturadecontaslegadooriginacao.config;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class FeignRetryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        var exception = FeignException.errorStatus(methodKey, response);
        if (HttpStatus.valueOf(response.status()).isSameCodeAs(HttpStatus.REQUEST_TIMEOUT)) {
            log.info("Status is 408. Retrying");
            return getRetryableException(response, exception);
        } else if (HttpStatus.valueOf(response.status()).is5xxServerError() ) {
            log.info("Status is 5xx. Retrying");
            return getRetryableException(response, exception);
        }
        return exception;
    }

    private static RetryableException getRetryableException(Response response, FeignException exception) {
        final Long nonRetryable = null;
        return new RetryableException(
                response.status(),
                "Client error: " + exception.getMessage(),
                response.request().httpMethod(),
                exception,
                nonRetryable,
                response.request());
    }
}