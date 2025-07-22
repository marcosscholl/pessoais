package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.ConsultarDadosAssociado;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultarDadosAssociadoBuilder extends ConsultarDadosAssociado {

    @Builder
    public ConsultarDadosAssociadoBuilder(String cpf) {
        super();
        this.filtroConsultarDadosAssociado = FiltroConsultarDadosAssociadoBuilder.builder().cpf(cpf).build();
    }
}
