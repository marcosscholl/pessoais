package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontaservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadoras;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.GetFontesPagadorasResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.aberturacontaservice.ObjectFactory;
import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Slf4j
@Component
public class AberturaContaServiceClient extends WebServiceGatewaySupport {
    private final String uri;

    public AberturaContaServiceClient(Jaxb2Marshaller aberturaContaServiceMarshaller,
                                     @Value("${webservice.soap.AberturaContaService}") String uri) {
        this.setMarshaller(aberturaContaServiceMarshaller);
        this.setUnmarshaller(aberturaContaServiceMarshaller);
        this.uri = uri;
    }

    public GetFontesPagadorasResponse consultarFontesPagadoras(GetFontesPagadoras getFontesPagadoras){
        var request = new ObjectFactory().createGetFontesPagadoras(getFontesPagadoras);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (GetFontesPagadorasResponse) response.getValue();
    }

}
