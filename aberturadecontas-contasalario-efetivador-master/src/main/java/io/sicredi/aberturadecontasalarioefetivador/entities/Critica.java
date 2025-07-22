package io.sicredi.aberturadecontasalarioefetivador.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Embeddable
public class Critica {

    private String codigo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoCritica tipo;

}
