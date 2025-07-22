package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NomeCompletoDTO(@JsonProperty("first") String nome, @JsonProperty("socialName") String nomeSocial) {
}