package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    private String id;
    private String chave;
    private String idPedido;
    private String idCadastro;
    private String tipo;
    private Integer versao;
    private String status;
    private LocalDateTime dataCriacao;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}