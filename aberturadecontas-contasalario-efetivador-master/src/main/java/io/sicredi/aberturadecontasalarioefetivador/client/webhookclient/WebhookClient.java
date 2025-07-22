package io.sicredi.aberturadecontasalarioefetivador.client.webhookclient;

import feign.Response;
import io.sicredi.aberturadecontasalarioefetivador.dto.SolicitacaoWebhookResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface WebhookClient {

    @RequestMapping(method = RequestMethod.OPTIONS, value = "")
    Response ping();
    @PostMapping
    Response processarRetornoWebhook(@RequestBody SolicitacaoWebhookResponseDTO resultado);
}
