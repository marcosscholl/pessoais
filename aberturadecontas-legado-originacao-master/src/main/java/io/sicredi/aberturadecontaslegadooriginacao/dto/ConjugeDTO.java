package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConjugeDTO(@JsonProperty("suid") String cpf,
                         @JsonProperty("name") String nome) {
}