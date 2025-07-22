package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CodigoEntidadeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name="gestentConectorClient", url="${sicredi.aberturadecontas-legado-originacao.client.rest.gestent-conector.url}",
        configuration = {FeignClientConfiguration.class})
public interface GestentConectorClient {

    @GetMapping("/entidade-sicredi-completo?page=0&pageSize=1&codigoTipoEntidade=AGENCIA")
    CodigoEntidadeDTO buscarCodigoEntidade(
            @RequestParam("codigoCooperativa") String codigoCooperativa,
            @RequestParam("codigoAgencia") String codigoAgencia);
}

