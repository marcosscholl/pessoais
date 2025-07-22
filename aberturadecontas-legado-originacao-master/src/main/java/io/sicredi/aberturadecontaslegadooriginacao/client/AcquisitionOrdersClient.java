package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionOrdersDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name="acquisitionOrdersClient", url="${sicredi.aberturadecontas-legado-originacao.client.rest.acquisition-orders.url}",
        configuration = {FeignClientConfiguration.class})
public interface AcquisitionOrdersClient {

    @GetMapping("/orders/{orderId}")
    AcquisitionOrdersDTO buscaPedido(@PathVariable("orderId") String orderId);

}