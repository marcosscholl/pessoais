package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.PedidoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name="originacaoLegadoClient", url="${sicredi.aberturadecontas-legado-originacao.client.rest.originacao-legado.url}",
        configuration = {FeignClientConfiguration.class})
public interface OriginacaoLegadoClient {

    @PostMapping("/processar")
    void processaOriginacao(@RequestBody PedidoDTO pedido);
}
