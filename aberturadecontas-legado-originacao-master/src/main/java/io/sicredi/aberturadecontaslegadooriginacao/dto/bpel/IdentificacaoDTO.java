package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record IdentificacaoDTO(String documento,
                               String tipo,
                               LocalDate dataEmissao,
                               String orgaoEmissor,
                               String estadoEmissao,
                               LocalDate dataValidade,
                               String origem) {
}