package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CadastroDTO(@JsonProperty("customerId") String idCadastro,
                          @JsonProperty("suid") String cpf,
                          @JsonProperty("role") Papel papel,
                          @JsonProperty("permissionLess") Boolean semPermissao,
                          @JsonProperty("mainHolder") Boolean titularPrincipal) {
}