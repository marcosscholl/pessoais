package io.sicredi.aberturadecontasalarioefetivador.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    @Bean
    RestClient restConfig(){
        return RestClient.builder().build();
    }

}