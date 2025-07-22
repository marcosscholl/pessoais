package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocoreeventos;

import io.sicredi.aberturadecontasalarioefetivador.config.FeignClientConfiguration;
import io.sicredi.aberturadecontasalarioefetivador.dto.ContaSalarioCoreEventosDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "ontaSalarioCoreEventosClient",
        url="${webservice.rest.contaSalarioCoreEventos}",
        configuration = FeignClientConfiguration.class
)
public interface ContaSalarioCoreEventosClient {

    @GetMapping("/eventos/agencia/{agencia}/conta/{conta}?tipos=ALTERACAO_PORTABILIDADE&tipos=ALTERACAO_CONVENIO&tipos=ENCERRAMENTO_CONTA_SALARIO")
    List<ContaSalarioCoreEventosDTO> buscarEventosContaSalario(
            @PathVariable("agencia") String agencia,
            @PathVariable("conta") String conta
    );
}
