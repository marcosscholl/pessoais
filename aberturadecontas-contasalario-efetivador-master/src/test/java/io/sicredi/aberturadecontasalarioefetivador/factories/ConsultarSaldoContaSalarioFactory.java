package io.sicredi.aberturadecontasalarioefetivador.factories;

import br.com.sicredi.contasalario.ejb.ConsultarSaldoContaSalarioResponse;
import br.com.sicredi.contasalario.ejb.OutConsultarSaldoContaSalario;
import br.com.sicredi.contasalario.ejb.SaldoContaSalarioDTO;
import io.sicredi.aberturadecontasalarioefetivador.client.contasalariocontasservice.dto.ConsultarSaldoContaSalarioDTO;

import java.math.BigDecimal;

public class ConsultarSaldoContaSalarioFactory {

    public static ConsultarSaldoContaSalarioDTO consultarSaldoContaSalarioRequest() {
        return ConsultarSaldoContaSalarioDTO.builder()
                .cooperativa("0167")
                .numeroConta("903677")
                .build();

    }
    public static ConsultarSaldoContaSalarioResponse consultarSaldoContaSalarioResponse() {
        SaldoContaSalarioDTO saldoContaSalarioDTO = new SaldoContaSalarioDTO();
        saldoContaSalarioDTO.setCodigoDestinoCredito("C");
        saldoContaSalarioDTO.setCodigoTipoConta("01");
        saldoContaSalarioDTO.setFlagTarifado("S");
        saldoContaSalarioDTO.setNumeroAgencia("1590");
        saldoContaSalarioDTO.setNumeroAgenciaUsuario("0167");
        saldoContaSalarioDTO.setNumeroBanco("336");
        saldoContaSalarioDTO.setNumeroContaDestino("502553");
        saldoContaSalarioDTO.setNumeroContaSalario("903677");
        saldoContaSalarioDTO.setNumeroEmpresaConveniada("3AO");
        saldoContaSalarioDTO.setNumeroPostoUsuario("04");
        saldoContaSalarioDTO.setQuantidadeExtratoMovimento(Short.valueOf("0"));
        saldoContaSalarioDTO.setQuantidadeExtratoSaldo(Short.valueOf("0"));
        saldoContaSalarioDTO.setValorProvisaoCpmf(BigDecimal.ZERO);
        saldoContaSalarioDTO.setValorSaldoAnterior(BigDecimal.ZERO);
        saldoContaSalarioDTO.setValorSaldoContaSalario(BigDecimal.valueOf(1000));
        saldoContaSalarioDTO.setValorSaldoMedio(BigDecimal.ZERO);

        OutConsultarSaldoContaSalario out = new OutConsultarSaldoContaSalario();
        out.setDadosAgenciaContaDTO(saldoContaSalarioDTO);

        ConsultarSaldoContaSalarioResponse response = new ConsultarSaldoContaSalarioResponse();
        response.setOutConsultarSaldoContaSalario(out);
        return response;
    }

    public static ConsultarSaldoContaSalarioResponse consultarSaldoContaSalarioEncerradaResponse() {
        ConsultarSaldoContaSalarioResponse saldoContaSalarioDTO = consultarSaldoContaSalarioResponse();
        saldoContaSalarioDTO.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO().setNumeroContaSalario("903676");
        //        saldoContaSalarioDTO.getOutConsultarSaldoContaSalario().getDadosAgenciaContaDTO().setd
        return saldoContaSalarioDTO;
    }
}
