package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CondicaoPessoalDTO(@JsonProperty("civilCapacity") CapacidadeCivil capacidadeCivil,
                                 @JsonProperty("condition") CondicaoPessoal condicao) {
}