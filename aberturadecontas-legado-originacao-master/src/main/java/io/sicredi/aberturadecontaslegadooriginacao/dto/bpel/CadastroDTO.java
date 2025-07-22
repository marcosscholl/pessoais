package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CadastroDTO(String id,
                          String coreId,
                          String cpf,
                          String nome,
                          LocalDate dataNascimento,
                          DadosCadastroDTO dadosCadastro,
                          Boolean criadoCoredb,
                          Boolean criadoSiebeldb) {
}