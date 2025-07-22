package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResidenciaExteriorDTO(@JsonProperty("id") String id,
                                    @JsonProperty("countryCode") String codigoPais,
                                    @JsonProperty("countryDescription") String descricaoPais,
                                    @JsonProperty("nif") String nif) {
}