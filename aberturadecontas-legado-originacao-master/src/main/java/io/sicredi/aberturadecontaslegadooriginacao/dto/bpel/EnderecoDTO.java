package io.sicredi.aberturadecontaslegadooriginacao.dto.bpel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnderecoDTO(String id,
                          String coreId,
                          String tipo,
                          String logradouro,
                          String tipoLogradouro,
                          Boolean semNumero,
                          String numero,
                          String complemento,
                          String bairro,
                          String cidade,
                          String estado,
                          String cep,
                          Integer codigoPais,
                          String descricaoPais,
                          Boolean enderecoPrincipal,
                          Boolean permiteCorrespondencia,
                          String origem,

                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
                          LocalDateTime dataAtualizacao,

                          @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
                          LocalDateTime dataCriacao) {
}