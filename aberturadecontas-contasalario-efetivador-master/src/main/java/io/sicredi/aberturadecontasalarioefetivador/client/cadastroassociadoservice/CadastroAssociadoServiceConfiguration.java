package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class CadastroAssociadoServiceConfiguration {

    @Bean
    public Jaxb2Marshaller cadastroAssociadoServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro");
        return marshaller;
    }

}
