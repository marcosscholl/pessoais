package io.sicredi.aberturadecontaslegadooriginacao.client;

import io.sicredi.aberturadecontaslegadooriginacao.config.FeignClientConfiguration;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DadosSimulacaoCestaRelacionamentoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name="monthlyFeeSimulation", url="${sicredi.aberturadecontas-legado-originacao.client.rest.monthly-fee-simulation.url}",
        configuration = {FeignClientConfiguration.class})
public interface MonthlyFeeSimulationClient {

    @GetMapping("simulations/{idSimulacao}")
    DadosSimulacaoCestaRelacionamentoDTO buscarDadosCestaRelacionamento(@PathVariable("idSimulacao") String idSimulacao);
}