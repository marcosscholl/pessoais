package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionOrdersDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.CustomerDataDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name="customerDataClient", url="${sicredi.aberturadecontas-legado-originacao.client.rest.customer-data.url}",
        configuration = {FeignClientConfiguration.class})
public interface CustomerDataClient {

    @GetMapping("/customers/{idCadastro}")
    CustomerDataDTO buscarDadosCliente(@PathVariable("idCadastro") String idCadastro);

}