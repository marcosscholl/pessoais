package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.URL;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfiguracaoDTO(@URL(message = "O campo 'urlWebhook' deve ser uma URL válida")
                              @NotBlank(message = "O campo 'urlWebhook' é obrigatório")
                              @Size(max = 150, message = "O campo 'urlWebhook' deve ter no máximo 150 caracteres")
                              String urlWebhook,
                              @Size(max = 8, message = "O campo 'portaHttp' deve ter no máximo 8 caracteres")
                              String portaHttp) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
