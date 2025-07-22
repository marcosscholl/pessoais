package io.sicredi.aberturadecontasalarioefetivador.client.contasalarioservice;

import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.ContaSalarioResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalario;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.CriarContaSalarioResponse;
import br.com.sicredi.mua.cadastro.business.server.ws.v1.contasalarioservice.ObjectFactory;
import io.sicredi.aberturadecontasalarioefetivador.dto.CriarContaSalarioResponseCustomizadoDTO;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.SOAPWebServiceRetryException;
import io.sicredi.aberturadecontasalarioefetivador.utils.Utils;
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
public class ContaSalarioServiceClient extends WebServiceGatewaySupport {
    public static final String ERRO_GENERICO_OSB = "OSB-382500";
    private final String uri;
    private final int period;
    private final int maxAttempts;

    public ContaSalarioServiceClient(Jaxb2Marshaller contaSalarioServiceMarshaller,
                                     Jaxb2Marshaller contaSalarioServiceMarshallerResponse,
                                     @Value("${webservice.soap.ContaSalarioService}") String uri,
                                     @Value("${webservice.soap.retryer.period}") int period,
                                     @Value("${webservice.soap.retryer.maxAttempts}") int maxAttempts) {
        this.setMarshaller(contaSalarioServiceMarshaller);
        this.setUnmarshaller(contaSalarioServiceMarshallerResponse);
        this.uri = uri;
        this.period = period;
        this.maxAttempts = maxAttempts;
    }

    public CriarContaSalarioResponse criarContaSalario(CriarContaSalario criarContaSalario) {
        log.info("Chamando criarContaSalario para o CPF {}...\n {}", criarContaSalario.getContaSalario().getNumCPF(), Utils.printJson(criarContaSalario));
        var request = new ObjectFactory().createCriarContaSalario(criarContaSalario);

        RetryTemplate retryTemplate = criaRetryTemplate();
        CriarContaSalarioResponse criarContaSalarioResponse = new CriarContaSalarioResponse();
        try {
            return retryTemplate.execute((RetryCallback<CriarContaSalarioResponse, Exception>) context -> {
                CriarContaSalarioResponseCustomizadoDTO response = (CriarContaSalarioResponseCustomizadoDTO)
                        getWebServiceTemplate().marshalSendAndReceive(uri, request);

                ContaSalarioResponse contaSalarioResponse = new ContaSalarioResponse();
                contaSalarioResponse.setCodConvenioFontePagadora(response.getCodConvenioFontePagadora());
                contaSalarioResponse.setNumCPF(response.getNumCPF());
                contaSalarioResponse.setNumCooperativa(response.getNumCooperativa());
                contaSalarioResponse.setNumAgencia(response.getNumAgencia());
                contaSalarioResponse.setNumConta(response.getNumConta());
                contaSalarioResponse.setCodStatus(response.getCodStatus());
                contaSalarioResponse.setDesStatus(response.getDesStatus());

                criarContaSalarioResponse.setContaSalarioResponse(contaSalarioResponse);

                if (ERRO_GENERICO_OSB.equals(contaSalarioResponse.getCodStatus())) {
                    log.warn("CriarContaSalario CPF {} - Erro {}. Realizando retry...",
                            criarContaSalario.getContaSalario().getNumCPF(), ERRO_GENERICO_OSB);
                    throw new SOAPWebServiceRetryException("CriarContaSalario - Erro " + ERRO_GENERICO_OSB);
                }

                return criarContaSalarioResponse;
            });
        } catch (Exception e) {
            log.error("CriarContaSalario CPF {} - Erro após tentativas.", criarContaSalario.getContaSalario().getNumCPF(), e);
            return criarContaSalarioResponse;
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
