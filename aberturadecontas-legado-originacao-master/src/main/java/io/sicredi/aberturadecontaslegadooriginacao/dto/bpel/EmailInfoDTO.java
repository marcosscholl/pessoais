package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailInfoDTO(String id,
                           String coreId,
                           String email,
                           Integer ordem,
                           Boolean verificado,
                           String tipo) {
}