package io.sicredi.aberturadecontaslegadooriginacao.controller;

import br.com.sicredi.framework.exception.BusinessException;
import br.com.sicredi.framework.web.spring.exception.BadRequestException;
import io.sicredi.aberturadecontaslegadooriginacao.chain.handler.OriginacaoFisitalLegadoHandler;
import io.sicredi.aberturadecontaslegadooriginacao.dto.DetalhesPedidoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.DiaUtilDTO;
import io.sicredi.aberturadecontaslegadooriginacao.dto.bpel.OriginacaoLegadoDTO;
import io.sicredi.aberturadecontaslegadooriginacao.service.OriginacaoLegadoService;
import io.sicredi.aberturadecontaslegadooriginacao.service.ProcessamentoProdutosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Objects;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/originacao-legado/")
public class OriginacaoLegadoController {

    private final OriginacaoFisitalLegadoHandler originacaoFisitalLegadoHandler;
    private final ProcessamentoProdutosService processamentoProdutosService;
    private final OriginacaoLegadoService originacaoLegadoService;

    @Operation(summary = "Recupera da base de dados, os dados da originação que foi salvo na base de dados após o processamento da mensagem do tópico [ acquisition-engine-manager-items-v1 ].")
    @GetMapping(path = "{idPedido}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Nenhuma originação encontrada na base de dados.")
    })
    public OriginacaoLegadoDTO buscarDadosOriginacaoLegado(@PathVariable(name = "idPedido") final String idPedido) {
        return originacaoFisitalLegadoHandler.buscarOriginacaoLegadoPorIdPedido(idPedido);
    }


    @Operation(summary = "Processamento de status produto podendo ser LIBERADO, CANCELADO ou FALHA")
    @PostMapping(path = "{idPedido}/item-pedido/{idItemPedido}/status/{status}")
    public void processarStatusProduto(@PathVariable(name = "idPedido") final String idPedido,
                                       @PathVariable(name = "idItemPedido") final String idItemPedido,
                                       @PathVariable(name = "status") final String status,
                                       @RequestBody(required = false) DetalhesPedidoDTO detalheCancelamento) {
        log.info("[{}] Solicitação de processamento de status item pedido. status: {}, idItemPedido: {}, detalheCancelamento: {}",
                idPedido, status, idItemPedido, Objects.requireNonNullElse(detalheCancelamento, ""));
        processamentoProdutosService.processarStatusPedido(idPedido, idItemPedido, status, detalheCancelamento);
    }

    @Operation(summary = "Sinalização de cancelamento de produto do pedido")
    @PostMapping(path = "{idPedido}/item-pedido/{idItemPedido}/cancelar-pedido")
    public void cancelarProduto(@PathVariable(name = "idPedido") final String idPedido,
                                @PathVariable(name = "idItemPedido") final String idItemPedido,
                                @RequestBody DetalhesPedidoDTO detalhesPedidoDTO) {
        log.info("[{}] Solicitação de cancelamento manual de item pedido. idItemPedido: {}, detalheCancelamento: {}",
                idPedido, idItemPedido, detalhesPedidoDTO);
        processamentoProdutosService.cancelarItemPedidoManual(idPedido, idItemPedido, detalhesPedidoDTO);
    }

    @Operation(summary = "Recupera o próximo dia útil e atualiza o dia do primeiro pagamento do produto CAPITAL_LEGACY.")
    @PostMapping(path = "{idPedido}/atualizar-dia-primeiro-pagamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proximo dia útil a partir do id do pedido informando."),
            @ApiResponse(responseCode = "400", description = "Caso a data consultada não esteja no padrão [yyyy-MM-dd]")
    })
    public DiaUtilDTO obterProximoDiaUtil(@Parameter(description = "Id do pedido que será atualizado e retornado o próximo dia útil.")
                                         @PathVariable(name = "idPedido") final String idPedido) {
        log.info("Solicitação do próximo dia útil para o pedido: [{}]", idPedido);
        try {
            LocalDate proximoDiaUtil = originacaoLegadoService.atualizarDiaPrimeiroPagamentoCapital(idPedido);
            return new DiaUtilDTO(proximoDiaUtil);
        } catch (BusinessException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}

