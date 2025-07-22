package io.sicredi.aberturadecontaslegadooriginacao.client;

import br.com.sicredi.crm.ws.v1.carteiraservice.ConsultarCarteiraRequest;
import br.com.sicredi.crm.ws.v1.carteiraservice.ConsultarCarteiraResponse;
import br.com.sicredi.crm.ws.v1.carteiraservice.ObjectFactory;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import java.math.BigDecimal;

@Component
public class CarteiraServiceSOAPClient extends WebServiceGatewaySupport {

    public CarteiraServiceSOAPClient(Jaxb2Marshaller adminServiceMarshaller,
                                     @Value("${sicredi.aberturadecontas-legado-originacao.client.soap.carteira-service.url}") String uri
    ) {
        this.setMarshaller(adminServiceMarshaller);
        this.setUnmarshaller(adminServiceMarshaller);
        this.setDefaultUri(uri);
    }

    public ConsultarCarteiraResponse obterCodigoCarteira(String idCarteira){
        var request = new ConsultarCarteiraRequest();
        request.setOidCadastroCarteira(new BigDecimal(idCarteira));
        var req = new ObjectFactory().createConsultarCarteiraRequest(request);
        var res = (JAXBElement<ConsultarCarteiraResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(req);

        return res.getValue();
    }
}
