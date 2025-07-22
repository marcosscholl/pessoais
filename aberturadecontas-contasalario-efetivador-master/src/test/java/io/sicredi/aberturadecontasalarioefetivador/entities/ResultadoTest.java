package io.sicredi.aberturadecontasalarioefetivador.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResultadoTest {

    @Test
    void mapRecebido() {
        Resultado resultado = Resultado.map("RECEBIDO");
        assertThat(resultado).isEqualTo(Resultado.RECEBIDO);
    }

    @Test
    void mapEmProcessamento() {
        Resultado resultado = Resultado.map("EM_PROCESSAMENTO");
        assertThat(resultado).isEqualTo(Resultado.EM_PROCESSAMENTO);
    }

    @Test
    void mapConcluido() {
        Resultado resultado = Resultado.map("CONCLUIDO");
        assertThat(resultado).isEqualTo(Resultado.CONCLUIDO);
    }

    @Test
    void mapConcluidoParcialmente() {
        Resultado resultado = Resultado.map("CONCLUIDO_PARCIALMENTE");
        assertThat(resultado).isEqualTo(Resultado.CONCLUIDO_PARCIALMENTE);
    }

    @Test
    void mapErro() {
        Resultado resultado = Resultado.map("ERRO");
        assertThat(resultado).isEqualTo(Resultado.ERRO);
    }
}