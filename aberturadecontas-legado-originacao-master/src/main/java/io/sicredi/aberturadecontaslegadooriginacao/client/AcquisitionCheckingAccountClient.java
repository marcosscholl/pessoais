package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.NumeroContaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "acquisitionCheckingAccount", url = "${sicredi.aberturadecontas-legado-originacao.client.rest.acquisition-checking-account.url}",
        configuration = {FeignClientConfiguration.class})
public interface AcquisitionCheckingAccountClient {

    @GetMapping("/checking-account/book-number")
    NumeroContaDTO buscarNumeroConta(@RequestParam("coop") String codigoCooperativa,
                                                  @RequestParam("orderId") String idPedido);
}