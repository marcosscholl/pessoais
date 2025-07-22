package io.sicredi.aberturadecontasalarioefetivador.client.telefoneservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.telefoneservice.*;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class TelefoneServiceClient extends WebServiceGatewaySupport {
    private final String uri;
    public TelefoneServiceClient(Jaxb2Marshaller telefoneServiceMarshaller,
                              @Value("${webservice.soap.TelefoneService}") String uri) {
        this.setMarshaller(telefoneServiceMarshaller);
        this.setUnmarshaller(telefoneServiceMarshaller);
        this.uri = uri;
    }

    public GetTelefonesResponse consultarTelefones(GetTelefones getTelefones){
        var request = new ObjectFactory().createGetTelefones(getTelefones);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (GetTelefonesResponse) response.getValue();
    }

    public SalvarTelefoneResponse salvarTelefone(SalvarTelefone salvarTelefone){
        var request = new ObjectFactory().createSalvarTelefone(salvarTelefone);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (SalvarTelefoneResponse) response.getValue();
    }


}
