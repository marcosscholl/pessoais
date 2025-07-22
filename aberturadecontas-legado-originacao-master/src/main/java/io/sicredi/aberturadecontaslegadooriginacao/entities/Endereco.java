package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    private String id;
    private String coreId;
    private String tipo;
    private String logradouro;
    private String tipoLogradouro;
    private Boolean semNumero;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private Integer codigoPais;
    private String descricaoPais;
    private Boolean enderecoPrincipal;
    private Boolean permiteCorrespondencia;
    private String origem;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime dataCriacao;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}