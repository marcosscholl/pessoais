package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.sicredi.aberturadecontasalarioefetivador.validation.FontePagadoraConstraint;
import io.sicredi.aberturadecontasalarioefetivador.validation.ListaCadastrosCPFUnicoConstraint;
import io.sicredi.aberturadecontasalarioefetivador.validation.ListaCadastrosMaxSizeConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@FontePagadoraConstraint
public record SolicitacaoRequestDTO(@NotBlank(message = "O campo 'numCooperativa' é obrigatório")
                                    @Pattern(regexp = "\\d{4}", message = "O campo 'numCooperativa' deve conter 4 dígitos")
                                    String numCooperativa,
                                    @NotBlank(message = "O campo 'numAgencia' é obrigatório")
                                    @Pattern(regexp = "^[a-zA-Z0-9]{2}$", message = "O campo 'numAgencia' deve conter 2 dígitos")
                                    String numAgencia,
                                    @NotBlank(message = "O campo 'codConvenioFontePagadora' é obrigatório")
                                    @Size(max = 7, message = "O campo 'codConvenioFontePagadora' deve ter no máximo 7 caracteres")
                                    String codConvenioFontePagadora,
                                    @Pattern(regexp = "\\d{14}", message = "O campo 'cnpjFontePagadora' deve conter 14 dígitos")
                                    @CNPJ(message = "O campo 'cnpjFontePagadora' deve conter um CNPJ válido")
                                    String cnpjFontePagadora,
                                    @Pattern(regexp = "\\d{11}", message = "O campo 'cpfFontePagadora' deve conter 11 dígitos")
                                    @CPF(message = "O campo 'cpfFontePagadora' deve conter um CPF válido")
                                    String cpfFontePagadora,
                                    @NotNull(message = "O campo 'cadastros' é obrigatório")
                                    @NotEmpty(message = "A lista 'cadastros' não pode ser vazia")
                                    @ListaCadastrosMaxSizeConstraint
                                    @ListaCadastrosCPFUnicoConstraint
                                    List<@Valid CadastroRequestDTO> cadastros,
                                    @Valid ConfiguracaoDTO configuracao) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
