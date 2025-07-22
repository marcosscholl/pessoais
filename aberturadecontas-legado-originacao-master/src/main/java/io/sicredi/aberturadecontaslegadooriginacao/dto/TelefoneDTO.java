package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelefoneDTO(@JsonProperty("id") String id,
                          @JsonProperty("allowSms") Boolean permiteSms,
                          @JsonProperty("countryCode") String codigoPais,
                          @JsonProperty("number") String numero,
                          @JsonProperty("phoneType") TipoTelefone tipo,
                          @JsonProperty("stateCode") String ddd) {
}