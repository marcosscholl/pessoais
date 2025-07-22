package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelefoneDTO(String id,
                          String coreId,
                          Boolean permiteSms,
                          String codigoPais,
                          String numero,
                          String tipo,
                          String ddd) {
}