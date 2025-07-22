package io.sicredi.aberturadecontaslegadooriginacao.event;

import br.com.sicredi.framework.web.spring.exception.InternalServerException;
import io.sicredi.aberturadecontaslegadooriginacao.dto.AcquisitionProductsCreationInputDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component()
public class AcquisitionProductsCreationInputProducer {

    private final StreamBridge streamBridge;

    public void send(AcquisitionProductsCreationInputDTO data) {
        log.info("[{}] enviando mensagem para acquisition-products-creation-input-v1. {}", data.idPedido(), data);

        Boolean mensagemEnviada = streamBridge.send("acquisitionProductsCreationInputProducer-out-0", data);

        if(Boolean.FALSE.equals(mensagemEnviada)){
            log.error("[{}] Não foi possivel enviar mensagem para acquisition-products-creation-input-v1", data.idPedido());
            throw new InternalServerException("Erro ao enviar a mensagem para tópico acquisition-products-creation-input-v1");
        }

    }

}
