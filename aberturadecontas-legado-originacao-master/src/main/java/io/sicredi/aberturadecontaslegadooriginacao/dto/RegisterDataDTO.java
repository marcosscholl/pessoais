package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sicredi.aberturadecontaslegadooriginacao.config.CustomLocalDateTimeIsoDeserializer;

import java.time.LocalDateTime;

public record RegisterDataDTO(@JsonProperty("id") String id,
                              @JsonProperty("key") String chave,
                              @JsonProperty("orderId") String idPedido,
                              @JsonProperty("customerId") String idCadastro,
                              @JsonProperty("type") String tipo,
                              @JsonProperty("migratedToCustomer") Boolean migradoParaCadastro,
                              @JsonProperty("version") Integer versao,
                              @JsonProperty("status") StatusDocumento status,
                              @JsonProperty("reused") Boolean reaproveitado,
                              @JsonProperty("source") String canal,
                              @JsonProperty("origin") String origem,
                              @JsonProperty("device") String dispositivo,
                              @JsonProperty("isRealTimeCaptured") Boolean capturadoEmTempoReal,
                              @JsonProperty("createdDate")
                              @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataCriacao
) {
}