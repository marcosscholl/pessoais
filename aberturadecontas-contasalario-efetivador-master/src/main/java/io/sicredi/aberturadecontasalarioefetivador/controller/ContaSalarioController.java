package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.ConsultarContaSalarioResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class ContaSalarioController {

    private static final String CABECALHO_CANAL = "Canal";
    private final ConsultarContaSalarioService consultarContaSalarioService;

    @GetMapping(path = "/conta-salario/documento/{documento}/{convenio}")
    @Operation(summary = "Busca detalhes de Conta Salário por documento e convênio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
    })
    public List<ConsultarContaSalarioResponseDTO> consultarContaSalario(@PathVariable("documento")
                                                                        @CPF(message = "O campo 'documento' deve conter um CPF válido")
                                                                        String documento,
                                                                        @PathVariable("convenio")
                                                                        @Size(max = 7, message = "O campo 'convenio' deve ter no máximo 7 caracteres")
                                                                        String convenio,
                                                                        @RequestHeader(name = CABECALHO_CANAL) String canal) {
        return consultarContaSalarioService.consultarContaSalario(documento, convenio, canal);
    }

}
