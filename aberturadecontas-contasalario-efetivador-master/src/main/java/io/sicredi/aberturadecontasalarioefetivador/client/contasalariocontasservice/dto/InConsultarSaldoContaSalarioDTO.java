package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.dto;

import br.com.sicredi.contasalario.ejb.InConsultarSaldoContaSalario;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class InConsultarSaldoContaSalarioDTO extends InConsultarSaldoContaSalario {

    @Builder
    public InConsultarSaldoContaSalarioDTO(ContaDTO conta) {
        super();
        this.conta = conta;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
