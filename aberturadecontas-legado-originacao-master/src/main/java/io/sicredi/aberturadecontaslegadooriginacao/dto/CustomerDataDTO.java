package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sicredi.aberturadecontaslegadooriginacao.config.CustomLocalDateTimeIsoDeserializer;

import java.time.LocalDateTime;

public record CustomerDataDTO(@JsonProperty("id") String id,
                              @JsonProperty("fromLegacy") Boolean origemLegado,
                              @JsonProperty("personal") DadosPessoaisDTO dadosPessoais,
                              @JsonProperty("personCondition") CondicaoPessoalDTO condicaoPessoal,
                              @JsonProperty("professional") DadosProfissionaisDTO dadosProfissionais,
                              @JsonProperty("registerDate")
                              @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataCriacao,
                              @JsonProperty("changeDate")
                              @JsonDeserialize(using = CustomLocalDateTimeIsoDeserializer.class) LocalDateTime dataAtualizacao,
                              @JsonProperty("status") String status) {
}