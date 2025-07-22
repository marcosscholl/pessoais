package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsultarContaSalarioResponseDTO(String nome,
                                               String documento,
                                               String conta,
                                               String agencia,
                                               String tipoContaSalario,
                                               String dataAbertura,
                                               String dataEncerramento,
                                               String status,
                                               String saldoAtual,
                                               String saldoAnterior,
                                               Convenio convenio,
                                               Portabilidade portabilidade,
                                               List<Alteracao> alteracoes
                                               ){

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Convenio(
            String codigo,
            String cnpj,
            String nome,
            String agencia,
            String conta

    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Portabilidade(
            String banco,
            String agencia,
            String conta,
            String tipoConta
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alteracao(
            String tipo,
            String dataAtualizacao,
            List<DadosAlterados> dadosAlterados
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DadosAlterados(
            String nome,
            String anterior,
            String atual
    ) {}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
