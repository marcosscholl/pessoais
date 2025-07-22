package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DadosRelacionamentoDTO(String cpf,
                                     String papel,
                                     Boolean semPoderes,
                                     Boolean titularPrincipal) {
}
