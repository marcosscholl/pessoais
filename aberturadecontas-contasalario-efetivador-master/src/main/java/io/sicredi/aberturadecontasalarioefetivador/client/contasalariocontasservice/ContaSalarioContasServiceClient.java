package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;
import br.com.sicredi.contasalario.ejb.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.dto.ConsultarSaldoContaSalarioDTO;
import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

@Slf4j
@Service
public class ContaSalarioContasServiceClient extends WebServiceGatewaySupport {

    private final String uri;
    private final int period;
    private final int maxAttempts;

    public ContaSalarioContasServiceClient(Jaxb2Marshaller contaSalarioContasServiceMarshaller,
                                           @Value("${webservice.soap.ContaSalarioContasService}") String uri,
                                           @Value("${webservice.soap.retryer.period}") int period,
                                           @Value("${webservice.soap.retryer.maxAttempts}") int maxAttempts) {
        this.setMarshaller(contaSalarioContasServiceMarshaller);
        this.setUnmarshaller(contaSalarioContasServiceMarshaller);
        this.uri = uri;
        this.period = period;
        this.maxAttempts = maxAttempts;
    }


    public ConsultarSaldoContaSalarioResponse consultarContaSalario(String cooperativa, String numeroConta) {
        log.info("Chamando consultarContaSalario para coop {}", cooperativa);
        var consultarSaldoContaSalario = ConsultarSaldoContaSalarioDTO.builder().cooperativa(cooperativa).numeroConta(numeroConta).build();
        var request = new ObjectFactory().createConsultarSaldoContaSalario(consultarSaldoContaSalario);

        RetryTemplate retryTemplate = criaRetryTemplate();
        try {
            return retryTemplate.execute(
                    (RetryCallback<ConsultarSaldoContaSalarioResponse, Exception>) context -> {
                        var response = (JAXBElement<?>) getWebServiceTemplate().marshalSendAndReceive(uri, request);
                        return (ConsultarSaldoContaSalarioResponse) response.getValue();
                    });
        } catch (Exception e) {
            log.error("Erro ao chamar consultarContaSalario para a coop {} - Erro após tentativas.", cooperativa, e);
            return new ConsultarSaldoContaSalarioResponse();
        }
    }

    protected RetryTemplate criaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(this.period);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(this.maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
