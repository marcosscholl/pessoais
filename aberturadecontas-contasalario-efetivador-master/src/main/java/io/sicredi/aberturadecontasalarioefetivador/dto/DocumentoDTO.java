package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.sicredi.aberturadecontasalarioefetivador.validation.DataEmFormatoValidoValidation;
import io.sicredi.aberturadecontasalarioefetivador.validation.DataPassadaValidation;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataEmFormatoValidoGroup;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataPassadaGroup;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataValidationGroups;
import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@GroupSequence({DocumentoDTO.class, DataValidationGroups.class})
public record DocumentoDTO(@NotBlank(message = "O campo 'numDocumento' é obrigatório")
                           String numDocumento,
                           @NotBlank(message = "O campo 'dataEmissaoDoc' é obrigatório")
                           @DataEmFormatoValidoValidation(message = "O campo 'dataEmissaoDoc' deve conter o formato dd/MM/yyyy",groups = DataEmFormatoValidoGroup.class)
                           @DataPassadaValidation(message= "O campo 'dataEmissaoDoc' deve ser uma data passada", groups = DataPassadaGroup.class)
                           String dataEmissaoDoc,
                           @NotBlank(message = "O campo 'nomeOrgaoEmissorDoc' é obrigatório")
                           String nomeOrgaoEmissorDoc,
                           @NotBlank(message = "O campo 'sglUfEmissorDoc' é obrigatório")
                           @Pattern(regexp = "^[A-Z]{2}$", message = "O campo 'sglUfEmissorDoc' deve conter exatamente 2 letras maiúsculas")
                           String sglUfEmissorDoc) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
