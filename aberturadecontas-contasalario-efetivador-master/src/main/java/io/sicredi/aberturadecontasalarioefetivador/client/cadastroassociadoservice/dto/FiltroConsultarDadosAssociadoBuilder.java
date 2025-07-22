package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.FiltroConsultarDadosAssociado;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FiltroConsultarDadosAssociadoBuilder extends FiltroConsultarDadosAssociado {

    @Builder
    public FiltroConsultarDadosAssociadoBuilder(String cpf) {
        super();
        this.nroDocumento = cpf;
        this.tpoDocumento = "CPF";
    }
}
