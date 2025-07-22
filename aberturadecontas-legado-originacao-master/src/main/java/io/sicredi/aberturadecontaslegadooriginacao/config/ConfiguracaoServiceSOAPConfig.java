package io.sicredi.aberturadecontaslegadooriginacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class ConfiguracaoServiceSOAPConfig {

    @Bean
    public Jaxb2Marshaller configSOAPMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.commons.business.server.ejb", "br.com.sicredi.crm.ws.v1.carteiraservice");
        return marshaller;
    }

}
