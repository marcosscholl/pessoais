package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DadosPessoais {

    private String nome;
    private String nomeSocial;
    private String cpf;
    private String naturalidadeCidade;
    private String naturalidadeEstado;
    private String nacionalidade;
    private LocalDate dataNascimento;
    private String genero;
    private String canalComunicacaoPreferencial;
    private String estadoCivil;
    private Boolean uniaoEstavel;
    private String regimeCasamento;
    private Conjuge conjuge;
    private Boolean residenciaExterior;
    private List<ResidenciaExterior> residenciasExterior;
    private List<Endereco> enderecos;
    private List<EmailInfo> emails;
    private Identificacao identificacao;
    private List<Parente> parentes;
    private List<Telefone> telefones;
    private List<Referencia> referencias;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}