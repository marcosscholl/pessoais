package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.RegisterDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Component
@FeignClient(name="acquisitionRegisterDataClient", url="${sicredi.aberturadecontas-legado-originacao.client.rest.acquisition-register-data.url}",
        configuration = {FeignClientConfiguration.class})
public interface AcquisitionRegisterDataClient {

    @GetMapping("/orders/{idPedido}/documents")
    List<RegisterDataDTO> buscarDocumentosDoPedido(@PathVariable("idPedido") String idPedido);

}