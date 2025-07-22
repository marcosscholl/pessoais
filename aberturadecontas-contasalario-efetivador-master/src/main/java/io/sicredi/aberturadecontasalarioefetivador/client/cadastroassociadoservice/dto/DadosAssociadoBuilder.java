package io.sicredi.aberturadecontasalarioefetivador.client.cadastroassociadoservice.dto;

import br.com.sicredi.servicos.cadastros.enterprise.ejb.cadastro.DadosAssociado;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.xml.datatype.XMLGregorianCalendar;

@Setter
@Getter
public class DadosAssociadoBuilder extends DadosAssociado {

    @Builder
    public DadosAssociadoBuilder(String nomAssociado, String nroDocumento, XMLGregorianCalendar datNascimento, long oidPessoa) {
        super();
        this.nomAssociado = nomAssociado;
        this.nroDocumento = nroDocumento;
        this.datNascimento = datNascimento;
        this.oidPessoa = oidPessoa;
    }
}
