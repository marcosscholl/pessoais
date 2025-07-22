package io.sicredi.aberturadecontasalarioefetivador.client.emailservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.emailservice.*;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class EmailServiceClient extends WebServiceGatewaySupport {
    private final String uri;
    public EmailServiceClient(Jaxb2Marshaller emailServiceMarshaller,
                                          @Value("${webservice.soap.EmailService}") String uri) {
        this.setMarshaller(emailServiceMarshaller);
        this.setUnmarshaller(emailServiceMarshaller);
        this.uri = uri;
    }

    public GetEmailsResponse consultarEmail(GetEmails getEmails) {
        var request = new ObjectFactory().createGetEmails(getEmails);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (GetEmailsResponse) response.getValue();
    }

    public SalvarEmailResponse salvarEmail(SalvarEmail salvarEmail) {
        var request = new ObjectFactory().createSalvarEmail(salvarEmail);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (SalvarEmailResponse) response.getValue();
    }
}
