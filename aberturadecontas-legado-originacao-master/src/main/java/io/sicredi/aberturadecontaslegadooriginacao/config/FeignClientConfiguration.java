package io.sicredi.aberturadecontaslegadooriginacao.config;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignClientConfiguration {

    @Value("${sicredi.aberturadecontas-legado-originacao.client.rest.config.retryer.period}")
    private final int period;
    @Value("${sicredi.aberturadecontas-legado-originacao.client.rest.config.retryer.maxPeriod}")
    private final int maxPeriod;
    @Value("${sicredi.aberturadecontas-legado-originacao.client.rest.config.retryer.maxAttempts}")
    private final int maxAttempts;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header("Accept", ContentType.APPLICATION_JSON.getMimeType());
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(period, maxPeriod, maxAttempts);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignRetryErrorDecoder();
    }

    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder();
    }
}