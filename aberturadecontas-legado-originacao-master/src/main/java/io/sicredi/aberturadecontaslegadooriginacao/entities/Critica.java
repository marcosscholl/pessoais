package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Critica {

    private EtapaProcessoOriginacao codigo;

    private String descricao;
    private String detalhe;

    public Critica(EtapaProcessoOriginacao codigo, String descricao){
        this.codigo = codigo;
        this.descricao = descricao;
    }

}
