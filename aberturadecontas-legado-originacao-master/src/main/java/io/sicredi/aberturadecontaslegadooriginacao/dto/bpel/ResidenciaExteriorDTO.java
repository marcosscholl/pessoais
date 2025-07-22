package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResidenciaExteriorDTO(String id,
                                    String coreId,
                                    String codigoPais,
                                    String descricaoPais,
                                    String nif) {
}