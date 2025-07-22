package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class AberturaContaServiceConfiguration {
    @Bean
    public Jaxb2Marshaller aberturaContaServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice");
        return marshaller;
    }
}
