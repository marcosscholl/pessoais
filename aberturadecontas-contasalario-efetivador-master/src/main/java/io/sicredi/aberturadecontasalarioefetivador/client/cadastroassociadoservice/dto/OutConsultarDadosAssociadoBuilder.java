package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.OutConsultarDadosAssociado;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OutConsultarDadosAssociadoBuilder extends OutConsultarDadosAssociado {

    @Builder
    public OutConsultarDadosAssociadoBuilder(List<DadosAssociado> elementos) {
        super();
        this.elementos = elementos;
    }
}
