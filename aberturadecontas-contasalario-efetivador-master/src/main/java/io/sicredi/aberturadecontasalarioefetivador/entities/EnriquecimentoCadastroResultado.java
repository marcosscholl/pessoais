package io.sicredi.aberturadecontasalarioefetivador.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnriquecimentoCadastroResultado {

    private boolean email;
    private boolean telefone;
    private boolean emailCriado;
    private boolean telefoneCriado;
    private List<String> criticas;
}
