package io.sicredi.aberturadecontasalarioefetivador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.sicredi.aberturadecontasalarioefetivador.validation.DataEmFormatoValidoValidation;
import io.sicredi.aberturadecontasalarioefetivador.validation.DataPassadaValidation;
import io.sicredi.aberturadecontasalarioefetivador.validation.TitularRepresentanteConstraint;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataEmFormatoValidoGroup;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataPassadaGroup;
import io.sicredi.aberturadecontasalarioefetivador.validation.group.DataValidationGroups;
import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.br.CPF;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@TitularRepresentanteConstraint
@GroupSequence({CadastroRequestDTO.class, DataValidationGroups.class})
public record CadastroRequestDTO(@NotBlank(message = "O campo 'cpf' é obrigatório")
                                 @CPF(message = "O campo 'cpf' deve conter um CPF válido")
                                 @Pattern(regexp = "\\d{11}", message = "O campo 'cpf' deve conter 11 dígitos")
                                 String cpf,
                                 String nome,
                                 @DataEmFormatoValidoValidation(message = "O campo 'dataNascimento' deve conter o formato dd/MM/yyyy", groups = DataEmFormatoValidoGroup.class)
                                 @DataPassadaValidation(message= "O campo 'dataNascimento' deve ser uma data passada", groups = DataPassadaGroup.class)
                                 String dataNascimento,
                                 @Pattern(regexp = "^[MF]$", message = "O campo 'flgSexo' deve conter 1 caracter 'M' ou 'F'")
                                 String flgSexo,
                                 @Email(message = "O campo 'email' deve conter um endereço de e-mail válido")
                                 String email,
                                 @Pattern(regexp = "\\d{10,11}", message = "O campo 'telefone' deve conter 10 ou 11 dígitos")
                                 String telefone,
                                 @Valid DocumentoDTO documento,
                                 @Valid EnderecoDTO endereco,
                                 @Valid PortabilidadeRequestDTO portabilidade,
                                 @Valid RepresentanteDTO representante) {
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
