package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DadosProfissionaisDTO(Boolean rendaNaoInformada,
                                    List<RendaDTO> rendas,
                                    OcupacaoDTO ocupacao,
                                    EmpregadorDTO empregador) {
}