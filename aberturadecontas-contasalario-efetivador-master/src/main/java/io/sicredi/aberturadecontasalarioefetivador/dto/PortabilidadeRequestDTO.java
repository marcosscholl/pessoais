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
public record PortabilidadeRequestDTO (@NotBlank(message = "O campo 'codBancoDestino' é obrigatório")
                                       @Pattern(regexp = "\\d{3}", message = "O campo 'codBancoDestino' deve conter 3 dígitos")
                                       String codBancoDestino,
                                       @NotBlank(message = "O campo 'numAgDestino' é obrigatório")
                                       @Size(max = 10, message = "O campo 'numAgDestino' deve conter no máximo 10 caracteres")
                                       String numAgDestino,
                                       @NotBlank(message = "O campo 'numContaDestino' é obrigatório")
                                       @Size(max = 20, message = "O campo 'numContaDestino' deve conter no máximo 20 caracteres")
                                       String numContaDestino,
                                       @NotBlank(message = "O campo 'tipoConta' é obrigatório")
                                       @Pattern(regexp = "\\d{2}", message = "O campo 'tipoConta' deve conter 2 dígitos")
                                       String tipoConta){
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
