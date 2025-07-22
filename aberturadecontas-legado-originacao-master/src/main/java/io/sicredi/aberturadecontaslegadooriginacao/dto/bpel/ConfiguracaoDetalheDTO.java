package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConfiguracaoDetalheDTO(CestaRelacionamentoDTO cestaRelacionamento,
                                     CapitalDTO capital) {
}