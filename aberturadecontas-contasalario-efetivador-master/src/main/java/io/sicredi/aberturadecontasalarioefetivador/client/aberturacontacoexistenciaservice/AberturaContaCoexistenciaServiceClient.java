package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice;


import br.com.sicredi.mua.cada.business.server.ejb.*;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class AberturaContaCoexistenciaServiceClient extends WebServiceGatewaySupport {
    private final String uri;

    public AberturaContaCoexistenciaServiceClient(Jaxb2Marshaller aberturaContaCoexistenciaServiceMarshaller,
                                      @Value("${webservice.soap.AberturaContaCoexistenciaService}") String uri) {
        this.setMarshaller(aberturaContaCoexistenciaServiceMarshaller);
        this.setUnmarshaller(aberturaContaCoexistenciaServiceMarshaller);
        this.uri = uri;
    }

    public GetContaSalarioResponse consultarContaSalario(GetContaSalario getContaSalario){
        var request = new ObjectFactory().createGetContaSalario(getContaSalario);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (GetContaSalarioResponse) response.getValue();
    }

    public GetInstituicaoFinanceiraResponse consultarInstituicaoFinanceira(){
        var request = new ObjectFactory().createGetInstituicaoFinanceira(new GetInstituicaoFinanceira());
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (GetInstituicaoFinanceiraResponse) response.getValue();
    }
}
