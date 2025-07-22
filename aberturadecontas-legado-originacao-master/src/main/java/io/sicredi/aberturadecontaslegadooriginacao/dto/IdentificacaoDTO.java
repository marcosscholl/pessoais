package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record IdentificacaoDTO(@JsonProperty("idNumber") String documento,
                               @JsonProperty("idType") String tipo,
                               @JsonProperty("issueDate") LocalDate dataEmissao,
                               @JsonProperty("validDate") LocalDate dataValidade,
                               @JsonProperty("issuingEntity") String orgaoEmissor,
                               @JsonProperty("issuingState") String estadoEmissao,
                               @JsonProperty("source") String origem) {
}