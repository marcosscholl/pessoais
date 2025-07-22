package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class TelefoneServiceConfiguration {

    @Bean
    public Jaxb2Marshaller telefoneServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice");
        return marshaller;
    }
}
