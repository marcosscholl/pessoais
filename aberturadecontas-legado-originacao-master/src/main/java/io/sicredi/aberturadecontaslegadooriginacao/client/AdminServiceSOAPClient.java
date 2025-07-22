package io.sicredi.aberturadecontaslegadooriginacao.client;

import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtil;
import br.com.sicredi.mua.commons.business.server.ejb.GetProximoDiaUtilResponse;
import br.com.sicredi.mua.commons.business.server.ejb.ObjectFactory;
import br.com.sicredi.mua.commons.business.server.ejb.OutProximoDiaUtil;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class AdminServiceSOAPClient extends WebServiceGatewaySupport {

    public AdminServiceSOAPClient(Jaxb2Marshaller adminServiceMarshaller,
                                  @Value("${sicredi.aberturadecontas-legado-originacao.client.soap.admin-service.url}") String uri
    ) {
        this.setMarshaller(adminServiceMarshaller);
        this.setUnmarshaller(adminServiceMarshaller);
        this.setDefaultUri(uri);
    }

    public GetProximoDiaUtilResponse getProximoDiaUtil(GetProximoDiaUtil request) {
        var req = new ObjectFactory().createGetProximoDiaUtil(request);
        var res = (JAXBElement<GetProximoDiaUtilResponse>) getWebServiceTemplate()
                .marshalSendAndReceive(req);

        return res.getValue();
    }

}
