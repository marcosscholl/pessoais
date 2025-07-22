package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class AberturaContaCoexistenciaServiceConfiguration {
    @Bean
    public Jaxb2Marshaller aberturaContaCoexistenciaServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.cada.business.server.ejb");
        return marshaller;
    }
}
