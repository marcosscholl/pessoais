package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmailDTO(@JsonProperty("id") String id,
                       @JsonProperty("email") String email,
                       @JsonProperty("order") Integer ordem,
                       @JsonProperty("verified") Boolean verificado) {
}