package io.sicredi.aberturadecontasalarioefetivador.client.aberturacontacoexistenciaservice.dto;

import br.com.sicredi.mua.cada.business.server.ejb.ConsultaSalarioFilter;
import br.com.sicredi.mua.cada.business.server.ejb.GetContaSalario;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetContaSalarioBuilder extends GetContaSalario {
    @Builder
    public GetContaSalarioBuilder(String branchCode, String conta, String cooperativa, String documento, long oidPessoa) {
        super();
        this.arg0 = new ConsultaSalarioFilter();
        this.arg0.setCodigoUA(branchCode);
        this.arg0.setConta(conta);
        this.arg0.setCooperativa(cooperativa);
        this.arg0.setCpfCnpj(documento);
        this.arg0.setOidPessoa(oidPessoa);
    }
}
