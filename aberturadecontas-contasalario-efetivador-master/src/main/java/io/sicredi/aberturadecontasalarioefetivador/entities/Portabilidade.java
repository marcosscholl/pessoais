package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "PORTABILIDADE")
public class Portabilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COD_BANCO_DESTINO")
    private String codBancoDestino;

    @Column(name = "NUM_AG_DESTINO")
    private String numAgDestino;

    @Column(name = "NUM_CONTA_DESTINO")
    private String numContaDestino;

    @Column(name = "TIPO_CONTA")
    @Enumerated(EnumType.STRING)
    private TipoConta tipoConta;
}
