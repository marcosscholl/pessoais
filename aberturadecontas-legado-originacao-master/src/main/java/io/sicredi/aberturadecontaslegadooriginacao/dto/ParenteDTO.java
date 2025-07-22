package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParenteDTO(@JsonProperty("id") String id,
                         @JsonProperty("name") String nome,
                         @JsonProperty("parentRole") TipoParente tipo) {
}