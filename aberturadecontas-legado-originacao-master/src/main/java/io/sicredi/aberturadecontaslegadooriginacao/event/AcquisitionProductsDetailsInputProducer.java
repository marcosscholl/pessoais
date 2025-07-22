package io.sicredi.aberturadecontaslegadooriginacao.event;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsDetailsInputDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component()
public class AcquisitionProductsDetailsInputProducer {

    private final StreamBridge streamBridge;

    public void send(AcquisitionProductsDetailsInputDTO data) {
        log.info("[{}] enviando mensagem para acquisition-products-details-input-v1. {}", data.idPedido(), data);

        Boolean mensagemEnviada = streamBridge.send("acquisitionProductsDetailsInputProducer-out-0", data);

        if(Boolean.FALSE.equals(mensagemEnviada)) {
            log.error("[{}] Não foi possivel enviar mensagem para acquisition-products-details-input-v1", data.idPedido());
            throw new InternalServerException("Erro ao enviar a mensagem para tópico acquisition-products-details-input-v1");
        }

    }

}
