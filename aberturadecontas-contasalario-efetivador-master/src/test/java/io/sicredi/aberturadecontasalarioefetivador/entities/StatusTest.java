package io.sicredi.aberturadecontasalarioefetivador.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatusTest {

    @Test
    void mapPedente() {
        Status status = Status.map("Pendente");
        assertThat(status).isEqualTo(Status.PENDENTE);
    }

    @Test
    void mapProcessando() {
        Status status = Status.map("PROCESSANDO");
        assertThat(status).isEqualTo(Status.PROCESSANDO);
    }

    @Test
    void mapPFinalizado() {
        Status status = Status.map("FINALIZADO");
        assertThat(status).isEqualTo(Status.FINALIZADO);
    }
}