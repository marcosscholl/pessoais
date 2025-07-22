package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociado;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociadoResponse;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ObjectFactory;
import jakarta.xml.bind.JAXBElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Component
public class CadastroAssociadoServiceClient extends WebServiceGatewaySupport {
    private final String uri;
    public CadastroAssociadoServiceClient(Jaxb2Marshaller cadastroAssociadoServiceMarshaller,
                                          @Value("${webservice.soap.CadastroAssociadoService}") String uri) {
        this.setMarshaller(cadastroAssociadoServiceMarshaller);
        this.setUnmarshaller(cadastroAssociadoServiceMarshaller);
        this.uri = uri;
    }

    public ConsultarDadosAssociadoResponse consultarDadosAssociado(ConsultarDadosAssociado consultarDadosAssociado){
        var request = new ObjectFactory().createConsultarDadosAssociado(consultarDadosAssociado);
        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
        return (ConsultarDadosAssociadoResponse) response.getValue();
    }
}
