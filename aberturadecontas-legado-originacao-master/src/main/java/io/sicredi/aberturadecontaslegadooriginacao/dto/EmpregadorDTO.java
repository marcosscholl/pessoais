package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmpregadorDTO(@JsonProperty("document") String documento,
                            @JsonProperty("name") String nome) {
}