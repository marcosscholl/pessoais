package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.dto.CanalRequestDTO;
import io.sicredi.aberturadecontasalarioefetivador.dto.CanalResponseDTO;
import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.service.CanalService;
import io.sicredi.aberturadecontasalarioefetivador.service.HeaderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestController
@RequestMapping("/canal")
@AllArgsConstructor
public class CanalController {

    private final CanalService canalService;
    private final HeaderService headerService;

    @PostMapping
    public ResponseEntity<?> criarCanal(@RequestBody CanalRequestDTO canal) {
        try {
            var novoCanal = canalService.criarCanal(Canal.builder()
                    .nome(canal.nome())
                    .documento(canal.documento())
                    .codigo(Objects.nonNull(canal.codigo())? Long.valueOf(canal.codigo()) : null)
                    .ativo(canal.ativo())
                    .build());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CanalResponseDTO.builder()
                            .nome(novoCanal.getNome())
                            .documento(novoCanal.getDocumento())
                            .ativo(novoCanal.isAtivo())
                            .codigo(novoCanal.getCodigo())
                            .build()
                    );
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getReason());
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> consultarCanal(@PathVariable Long codigo) {
        try {
            return ResponseEntity.ok(canalService.consultarCanal(codigo)
                    .map(canal -> CanalResponseDTO.builder()
                            .nome(canal.getNome())
                            .documento(canal.getDocumento())
                            .ativo(canal.isAtivo())
                            .codigo(canal.getCodigo())
                            .build())
                    .orElse(null)
            );
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getReason());
        }
    }
    @PutMapping("/{codigo}/desativar")
    public ResponseEntity<?> desativarCanal(@PathVariable Long codigo) {
        try {
            canalService.desativarCanal(codigo);
            return ResponseEntity.ok("Canal desativado com sucesso.");
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getReason());
        }
    }

    @PostMapping("/validar/{transactionId}/{canal}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> validarTransactionId(@PathVariable String transactionId, @PathVariable String canal) {
            headerService.validarHeaderSolicitacao(transactionId, canal);
            return ResponseEntity.ok().build();
    }
}
