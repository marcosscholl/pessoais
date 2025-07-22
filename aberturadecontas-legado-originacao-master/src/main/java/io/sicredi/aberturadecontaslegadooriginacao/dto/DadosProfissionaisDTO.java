package io.sicredi.aberturadecontaslegadooriginacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DadosProfissionaisDTO(@JsonProperty("employer") EmpregadorDTO empregador,
                                    @JsonProperty("incomeNotInformed") Boolean rendaNaoInformada,
                                    @JsonProperty("incomes") List<RendaDTO> rendas,
                                    @JsonProperty("occupation") OcupacaoDTO ocupacao,
                                    @JsonProperty("occupationCode") String codigoOcupacao) {
}