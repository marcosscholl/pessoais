package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CodigoEntidadeDTO(@JsonProperty("content") List<EntidadeDTO> codigoEntidade) {
}