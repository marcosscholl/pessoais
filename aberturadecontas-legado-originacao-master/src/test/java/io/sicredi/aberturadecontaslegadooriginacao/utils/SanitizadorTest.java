package io.sicredi.aberturadecontaslegadooriginacao.utils;

import io.sicredi.aberturadecontaslegadooriginacao.util.Sanitizador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ExtendWith(MockitoExtension.class)
public class SanitizadorTest {

    @Test
    @DisplayName("Deve retornar a string sanitizada quando estiver algum caracter especial")
    public void deveRetornarStringSanitizadaRemovendoAcentos() {
        var stringTeste = "Lêandro JOÃO-CARLOS DO PÁRA Caçula João Márcio müller muñoz 12ÀÈÌÒÙàèìòùÁÉÍÓÚáéíóúÃÕãõÂÊÎÔÛâêîôûÄËÏÖÜäëïöüŸÿÑñÅåÇç Ææ¢£¥ªº¿½¼¡±!#$%&'()*+-[]^_¨`<=>?@{|}/\\";
        var resultado = Sanitizador.sanitizar(stringTeste);
        assertEquals("Leandro JOAOCARLOS DO PARA Cacula Joao Marcio muller munoz AEIOUaeiouAEIOUaeiouAOaoAEIOUaeiouAEIOUaeiouYyNnAaCc", resultado);
    }

    @Test
    @DisplayName("Deve retornar a null quando não informar nenhum valor para sanitizar")
    public void deveRetornarNullCasoNaoSejaInformadoNenhumValorParaSanitizar() {
        var resultado = Sanitizador.sanitizar(null);
        assertNull(resultado);
    }

    @Test
    @DisplayName("Deve retornar o mesmo valor quando não tiver nenhum caracter especial na string")
    public void deveRetornarMesmoValorQuantoNaoTiverNenhumValorParaSanitizar() {
        var valorTest = "Hoje o dia esta lindo";
        var resultado = Sanitizador.sanitizar(valorTest);
        assertEquals(valorTest, resultado);
    }
}
