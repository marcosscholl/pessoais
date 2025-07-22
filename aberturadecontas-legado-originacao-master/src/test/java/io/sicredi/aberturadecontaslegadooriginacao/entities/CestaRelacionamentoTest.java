package io.sicredi.aberturadecontaslegadooriginacao.entities;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CestaRelacionamentoTest {

    @Test
    @DisplayName("Deve retornar verdadeiro quando todos os campos obrigatórios estão preenchidos")
    void deveRetornarVerdadeiroQuandoTodosCamposObrigatoriosEstaoPreenchidos() {
        CestaRelacionamento cestaRelacionamento = Instancio.of(CestaRelacionamento.class).create();
        assertTrue(cestaRelacionamento.valido());
    }

    @Test
    @DisplayName("Deve retornar falso quando algum campo obrigatório não está preenchido")
    void deveRetornarFalsoQuandoAlgumCampoObrigatorioNaoEstaPreenchido() {
        CestaRelacionamento cestaRelacionamento = Instancio.of(CestaRelacionamento.class).create();
        cestaRelacionamento.setDiaPagamento(null);
        assertFalse(cestaRelacionamento.valido());
    }

    @Test
    @DisplayName("Deve retornar falso quando todos os campos da cesta de relacionamento está nulos")
    void deveRetornarFalsoQuandoTodosOsCamposEstiveremNull() {
        CestaRelacionamento cestaRelacionamento = new CestaRelacionamento();
        assertFalse(cestaRelacionamento.valido());
    }
}



