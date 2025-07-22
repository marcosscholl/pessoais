package io.sicredi.aberturadecontasalarioefetivador.client.bureaurf;

import io.sicredi.aberturadecontasalarioefetivador.config.FeignClientConfiguration;
import io.sicredi.aberturadecontasalarioefetivador.dto.BureauRFDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Component
@FeignClient(name="bureauRFClient", url="${webservice.rest.bureauRF}", configuration = {FeignClientConfiguration.class})
public interface BureauRFClient {

    String AGENCIA_AREA_HEADER = "agenciaArea";
    String AUTORIZA_ONLINE_HEADER = "autorizaOnline";
    String AUTORIZA_ONLINE_VALUE = "true";
    String COOPERATIVA_HEADER = "cooperativa";
    String SISTEMA_ORIGEM_HEADER = "sistemaOrigem";
    String USUARIO_LOGADO_HEADER = "usuarioLogado";
    String USUARIO_LOGADO_VALUE = "APP_API_CTA_SALARIO";
    String SISTEMA_ORIGEM = "CONTASALARIO_EFETIVADOR";


    default BureauRFDTO consultaCPF(String cpf, String agenciaArea, String numCooperativa){
        return consultaCPF(cpf, agenciaArea, numCooperativa, AUTORIZA_ONLINE_VALUE,
                SISTEMA_ORIGEM, USUARIO_LOGADO_VALUE);
    }

    @GetMapping(value = "/{cpf}")
    BureauRFDTO consultaCPF(@PathVariable("cpf") String cpf,
                            @RequestHeader(AGENCIA_AREA_HEADER) String agenciaArea,
                            @RequestHeader(COOPERATIVA_HEADER) String numCooperativa,
                            @RequestHeader(AUTORIZA_ONLINE_HEADER) String autorizaOnline,
                            @RequestHeader(SISTEMA_ORIGEM_HEADER) String sistemaOrigem,
                            @RequestHeader(USUARIO_LOGADO_HEADER) String usuarioLogado);
}
