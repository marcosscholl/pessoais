package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NumeroContaDTO(@JsonProperty("account") String numerConta) {
}
