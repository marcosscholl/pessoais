package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadocontas;

import io.sicredi.aberturadecontasalarioefetivador.config.FeignClientConfiguration;
import io.sicredi.aberturadecontasalarioefetivador.dto.CadastroAssociadoContasDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "cadastroAssociadoContasClient",
        url = "${webservice.rest.cadastroassociadocontas}",
        configuration = FeignClientConfiguration.class
)
public interface CadastroAssociadoContasClient {

    @GetMapping("/associado-contas/associados/{documento}/contas")
    List<CadastroAssociadoContasDTO> buscarContas(
            @PathVariable("documento") String documento,
            @RequestParam("cooperativa") String cooperativa,
            @RequestParam("statusConta") String statusConta,
            @RequestParam("tipoConta") String tipoConta,
            @RequestParam("tipoRelacionamento") String tipoRelacionamento
    );

    @GetMapping("/associado-contas/associados/{documento}/contas")
    List<CadastroAssociadoContasDTO> buscarContasSalarioAssociado(
            @PathVariable("documento") String documento,
            @RequestParam("tipoConta") String tipoConta,
            @RequestParam("tipoRelacionamento") String tipoRelacionamento
    );

    @GetMapping("/associado-contas/associados/{documento}/contas")
    List<CadastroAssociadoContasDTO> buscarContasAtivas(
            @PathVariable("documento") String documento,
            @RequestParam("statusConta") String statusConta
    );
}