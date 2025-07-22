package io.sicredi.aberturadecontaslegadooriginacao.entities;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CadastroTest {

    @Test
    @DisplayName("Deve retornar verdadeiro quando todos os campos obrigatórios estão preenchidos")
    void deveRetornarVerdadeiroQuandoTodosCamposObrigatoriosEstaoPreenchidos() {
        Cadastro cadastro = Instancio.of(Cadastro.class).create();
        assertTrue(cadastro.isValido());
    }

    @Test
    @DisplayName("Deve retornar falso quando algum campo obrigatório não está preenchido")
    void deveRetornarFalsoQuandoAlgumCampoObrigatorioNaoEstaPreenchido() {
        Cadastro cadastro = Instancio.of(Cadastro.class).create();
        cadastro.setCpf(null);
        assertFalse(cadastro.isValido());
    }

    @Test
    @DisplayName("Deve retornar falso quando todos os campos do cadastro está nulos")
    void deveRetornarFalsoQuandoTodosOsCamposEstiveremNull() {
        Cadastro cadastro = new Cadastro();
        assertFalse(cadastro.isValido());
    }
}



