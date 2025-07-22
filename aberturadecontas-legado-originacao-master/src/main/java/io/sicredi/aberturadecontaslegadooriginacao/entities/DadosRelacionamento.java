package io.sicredi.aberturadecontaslegadooriginacao.entities;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DadosRelacionamento {
    private String cpf;
    private String papel;
    private Boolean semPoderes;
    private Boolean titularPrincipal;
}
