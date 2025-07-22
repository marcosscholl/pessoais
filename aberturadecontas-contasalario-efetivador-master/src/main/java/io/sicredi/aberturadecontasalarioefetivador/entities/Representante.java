package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Builder
public class Representante {

    @Column(name = "CPF_REPRESENTANTE")
    private String cpf;

    @Column(name= "NOME_REPRESENTANTE")
    private String nome;
}
