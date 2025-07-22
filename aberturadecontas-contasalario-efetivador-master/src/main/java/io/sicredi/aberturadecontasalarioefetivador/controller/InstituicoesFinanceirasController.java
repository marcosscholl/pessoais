package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarInstituicaoFinanceiraResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.service.AberturaContaCoexistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InstituicoesFinanceirasController {

    public static final String CABECALHO_EXTERNO_CANAL = "Canal";

    private final AberturaContaCoexistenciaService aberturaContaCoexistenciaService;


    @GetMapping(path = "/instituicoes-financeiras")
    @Operation(summary = "Consulta da lista de Instituições Financeiras autorizadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    public List<ConsultarInstituicaoFinanceiraResponseDTO> consultarInstituicoesFinanceiraAutorizadas(@RequestHeader(name = CABECALHO_EXTERNO_CANAL) String canal) {
        return aberturaContaCoexistenciaService.consultarInstituicoesFinanceiraAutorizadas();
    }

}
