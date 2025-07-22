package io.sicredi.aberturadecontasalarioefetivador.client.contasalarioservice;

import io.sicredi.aberturadecontasalarioefetivador.dto.CriarContaSalarioResponseCustomizadoDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class ContaSalarioServiceConfiguration {

    @Bean
    public Jaxb2Marshaller contaSalarioServiceMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice");
        return marshaller;
    }
    @Bean
    public Jaxb2Marshaller contaSalarioServiceMarshallerResponse() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(CriarContaSalarioResponseCustomizadoDTO.class);
        return marshaller;
    }

}