package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.dto;

import br.com.sicredi.contas.conta.cmodel.v1.Conta;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class ContaDTO extends Conta {

    @Builder
    public ContaDTO(String cooperativa, String numeroConta) {
        super();
        this.codigoAgencia = cooperativa;
        this.numero = Long.parseLong(numeroConta);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
