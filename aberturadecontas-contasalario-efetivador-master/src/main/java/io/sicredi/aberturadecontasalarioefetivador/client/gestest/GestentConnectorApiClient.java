package io.sicredi.aberturadecontasalarioefetivador.client.gestest;

import io.sicredi.aberturadecontasalarioefetivador.config.FeignClientConfiguration;
import io.sicredi.aberturadecontasalarioefetivador.dto.GestentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "gententConectorApiClient",
        url="${webservice.rest.gestentConectorApi}",
        configuration = FeignClientConfiguration.class
)
public interface GestentConnectorApiClient {

    @GetMapping("/entidade-sicredi")
    GestentDTO getEntidadeSicredi(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("codigoTipoEntidade") String codigoTipoEntidade,
            @RequestParam("codigoAgencia") String codigoAgencia,
            @RequestParam("codigoCooperativa") String codigoCooperativa,
            @RequestParam("codigoSituacao") String codigoSituacao
    );
}
