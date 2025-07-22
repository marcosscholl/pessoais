package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "ENDERECO")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TIPO_LOGRADOURO")
    private String tipoLogradouro;
    @Column(name = "NOME_LOGRADOURO")
    private String nomeLogradouro;
    @Column(name = "NUM_ENDERECO")
    private String numEndereco;
    @Column(name = "TXT_COMPLEMENTO")
    private String txtComplemento;
    @Column(name = "NOME_BAIRRO")
    private String nomeBairro;
    @Column(name = "NUM_CEP")
    private String numCep;
    @Column(name = "NOME_CIDADE")
    private String nomeCidade;
    @Column(name = "SGL_UF")
    private String sglUf;
}