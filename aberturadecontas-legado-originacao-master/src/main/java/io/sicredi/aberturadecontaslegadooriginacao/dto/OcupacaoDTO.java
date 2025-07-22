package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sicredi.aberturadecontaslegadooriginacao.config.CustomLocalDateTimeIsoDeserializer;

import java.time.LocalDateTime;

public record OcupacaoDTO(@JsonProperty("code") String codigo,
                          @JsonProperty("description") String descricao,
                          @JsonProperty("registerDate")
                          @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataCriacao,
                          @JsonProperty("changeDate")
                          @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataAtualizacao) {
}