package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CestaRelacionamento {

    private String id;
    private String diaPagamento;

    protected Boolean valido(){
        return Objects.nonNull(id) && Objects.nonNull(diaPagamento);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}