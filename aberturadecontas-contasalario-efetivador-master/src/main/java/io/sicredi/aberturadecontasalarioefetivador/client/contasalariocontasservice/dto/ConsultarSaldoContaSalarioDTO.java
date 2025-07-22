package io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.dto;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalario;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class ConsultarSaldoContaSalarioDTO extends ConsultarSaldoContaSalario {

    @Builder
    public ConsultarSaldoContaSalarioDTO(String cooperativa, String numeroConta) {
        super();
        this.inConsultarSaldoContaSalario = InConsultarSaldoContaSalarioDTO.builder()
                .conta(ContaDTO.builder()
                        .numeroConta(numeroConta)
                        .cooperativa(cooperativa)
                        .build())
                .build();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
