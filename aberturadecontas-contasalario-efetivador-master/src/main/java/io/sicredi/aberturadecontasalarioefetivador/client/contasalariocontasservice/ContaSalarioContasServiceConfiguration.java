package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class ContaSalarioContasServiceConfiguration {

    @Bean
    public Jaxb2Marshaller contaSalarioContasServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.contasalario.ejb");
        return marshaller;
    }

}