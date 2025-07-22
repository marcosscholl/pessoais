package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReferenciaDTO(@JsonProperty("id") String id,
                            @JsonProperty("name") String nome,
                            @JsonProperty("phone") String telefone) {
}