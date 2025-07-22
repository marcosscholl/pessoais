package io.sicredi.aberturadecontasalarioefetivador.config;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignRetryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        FeignException exception = feign.FeignException.errorStatus(methodKey, response);
        if (response.status() == 408) {
            //Bureau RF
            log.info("Status is 408. Retrying");
            return getRetryableException(response, exception);
        } else if (response.status() >= 500 && response.status() <= 599) {
            //Gestent
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
