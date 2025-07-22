package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosRelacionamentoDTO(@JsonProperty("suid") String cpf,
                                     @JsonProperty("role") String papel,
                                     @JsonProperty("permissionLess") boolean temPoderes) {
}
