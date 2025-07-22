package io.sicredi.aberturadecontasalarioefetivador.dto;

import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder(toBuilder = true)
public record BureauRFDTO(String anoObrito,
                          String codigoSituacaoCadastral,
                          String dataAtualizacao,
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento,
                          String descSituacaoCadastral,
                          String erro,
                          String nome,
                          String nomeMae,
                          String residenteExterior,
                          String sexo, String situacaoCadastral) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}

