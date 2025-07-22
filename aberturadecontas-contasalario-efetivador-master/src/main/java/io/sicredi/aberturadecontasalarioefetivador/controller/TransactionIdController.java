package io.sicredi.aberturadecontasalarioefetivador.controller;

import io.sicredi.aberturadecontasalarioefetivador.service.TransactionIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/transactionId")
@RequiredArgsConstructor
public class TransactionIdController {

    private final TransactionIdService transactionIdService;

    @PostMapping("/gerar/folha_ib")
    public ResponseEntity transactionId() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(transactionIdService.criaTransactionId());
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ex.getReason());
        }
    }

}
