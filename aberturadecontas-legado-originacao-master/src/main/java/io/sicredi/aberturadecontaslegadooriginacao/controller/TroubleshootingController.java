package io.sicredi.aberturadecontaslegadooriginacao.controller;

import io.sicredi.aberturadecontaslegadooriginacao.chain.OriginacaoFisitalLegadoChain;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.OriginacaoFisitalLegadoHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionEngineManagerItemsEventDTO;
import io.sicredi.aberturadecontaslegadooriginacao.json.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/troubleshooting/")
public class TroubleshootingController {

    private final OriginacaoFisitalLegadoChain originacaoFisitalLegadoChain;
    private final OriginacaoFisitalLegadoHandler originacaoFisitalLegadoHandler;

    @Operation(summary = "Recupera da base de dados o registro salvo na base de dados da originação processada.")
    @GetMapping(path = "originacao-legado/{idPedido}", produces = "application/json")
    public String getOrigicacaoLegado(@PathVariable(name = "idPedido") final String idPedido) {
        log.info("[{}] - Consultando os dados de orignação legado.", idPedido);
        return originacaoFisitalLegadoHandler.buscarDadosEntidadeOriginacaoLegadoPorIdPedido(idPedido);
    }

    @Operation(summary = "Recupera da base de dados, os dados da originação que foi salvo na base de dados após o processamento da mensagem do tópico [ acquisition-engine-manager-items-v1 ].")
    @PostMapping(path = "originacao-legado")
    public void reprocessarMenssagem(@Valid @RequestBody final AcquisitionEngineManagerItemsEventDTO acquisitionEngineManagerItemsDTO) {
        log.info("[{}] - Reprocessando originaçao fisital legado. {}", acquisitionEngineManagerItemsDTO.idPedido(), JsonUtils.objetoParaJson(acquisitionEngineManagerItemsDTO));
        originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(acquisitionEngineManagerItemsDTO);
    }

    @Operation(summary = "Reprocessa o evento de originação fisital recebido no tópico [ acquisition-engine-manager-items-v1 ] do kafka.")
    @PostMapping(path = "originacao-legado/processar/{idPedido}")
    public void processarEvento(@PathVariable(name = "idPedido") final String idPedido) {
        log.info("[{}] - Reprocessando originaçao fisital legado.", idPedido);
        originacaoFisitalLegadoChain.processaOriginacaoFisitalLegado(new AcquisitionEngineManagerItemsEventDTO(null, idPedido, null, null));
    }

    @Operation(summary = "Recebe um objeto e faz o log do mesmo - Criado para debug do BPEL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/log-object")
    public ResponseEntity<Void> logarObjeto(@RequestBody Object objeto){
        log.info("Objeto recebido na requisição: {}", JsonUtils.objetoParaJson(objeto));
        return ResponseEntity.ok().build();
    }
}

