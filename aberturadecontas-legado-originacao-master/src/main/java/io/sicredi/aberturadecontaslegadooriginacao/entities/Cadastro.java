package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cadastro {

    @Indexed(sparse = true, background = true)
    private String id;
    @Indexed(sparse = true, background = true)
    private String coreId;
    @Indexed(sparse = true, background = true)
    private String cpf;
    private String nome;
    private LocalDate dataNascimento;
    private DadosCadastro dadosCadastro;
    private Boolean criadoCoredb;
    private Boolean criadoSiebeldb;

    public boolean isValido() {
        return id != null && !id.isEmpty() &&
               cpf != null && !cpf.isEmpty() &&
               nome != null && !nome.isEmpty() &&
               dataNascimento != null &&
               dadosCadastro != null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}