package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnderecoDTO(@NotBlank(message = "O campo 'tipoLogradouro' é obrigatório")
                          @Size(max = 30, message = "O campo 'tipoLogradouro' deve ter no máximo 30 caracteres")
                          String tipoLogradouro,
                          @NotBlank(message = "O campo 'nomeLogradouro' é obrigatório")
                          @Size(max = 100, message = "O campo 'nomeLogradouro' deve ter no máximo 100 caracteres")
                          String nomeLogradouro,
                          @NotBlank(message = "O campo 'numEndereco' é obrigatório")
                          @Size(max = 30, message = "O campo 'numEndereco' deve ter no máximo 30 caracteres")
                          String numEndereco,
                          @Size(max = 100, message = "O campo 'txtComplemento' deve ter no máximo 100 caracteres")
                          String txtComplemento,
                          @NotBlank(message = "O campo 'nomeBairro' é obrigatório")
                          @Size(max = 50, message = "O campo 'nomeBairro' deve ter no máximo 50 caracteres")
                          String nomeBairro,
                          @NotBlank(message = "O campo 'numCep' é obrigatório")
                          @Pattern(regexp = "\\d{8}", message = "O campo 'numCep' deve conter 8 dígitos")
                          String numCep,
                          @NotBlank(message = "O campo 'nomeCidade' é obrigatório")
                          @Size(max = 30, message = "O campo 'nomeCidade' deve ter no máximo 30 caracteres")
                          String nomeCidade,
                          @NotBlank(message = "O campo 'sglUf' é obrigatório")
                          @Pattern(regexp = "^[A-Z]{2}$", message = "O campo 'sglUf' deve conter exatamente 2 letras maiúsculas")
                          String sglUf) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
