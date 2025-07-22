package io.sicredi.aberturadecontasalarioefetivador.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ContaSalarioEventoDestinoCreditoTest {

    @Test
    public void testGetCodigo() {
        assertEquals("A", ContaSalarioEventoDestinoCredito.SAQUE.getCodigo());
        assertEquals("B", ContaSalarioEventoDestinoCredito.CONTA_CORRENTE_SICREDI.getCodigo());
        assertEquals("C", ContaSalarioEventoDestinoCredito.OUTRA_IF.getCodigo());
        assertEquals("D", ContaSalarioEventoDestinoCredito.CONTA_POUPANCA_SICREDI.getCodigo());
        assertEquals("W", ContaSalarioEventoDestinoCredito.CONTA_DIGITAL_SICREDI.getCodigo());
        assertEquals("99", ContaSalarioEventoDestinoCredito.OUTRO.getCodigo());
    }

    @Test
    public void testGetDescricao() {
        assertEquals("MODALIDADE_SAQUE", ContaSalarioEventoDestinoCredito.SAQUE.getDescricao());
        assertEquals("PORTABILIDADE_CONTA_CORRENTE_SICREDI", ContaSalarioEventoDestinoCredito.CONTA_CORRENTE_SICREDI.getDescricao());
        assertEquals("PORTABILIDADE_OUTRA_INSTITUICAO", ContaSalarioEventoDestinoCredito.OUTRA_IF.getDescricao());
        assertEquals("PORTABILIDADE_CONTA_POUPANCA_SICREDI", ContaSalarioEventoDestinoCredito.CONTA_POUPANCA_SICREDI.getDescricao());
        assertEquals("PORTABILIDADE_CONTA_DIGITAL_SICREDI", ContaSalarioEventoDestinoCredito.CONTA_DIGITAL_SICREDI.getDescricao());
        assertEquals("OUTRO", ContaSalarioEventoDestinoCredito.OUTRO.getDescricao());
    }

    @Test
    public void testMapValidCodigo() {
        assertEquals(ContaSalarioEventoDestinoCredito.SAQUE, ContaSalarioEventoDestinoCredito.map("A"));
        assertEquals(ContaSalarioEventoDestinoCredito.CONTA_CORRENTE_SICREDI, ContaSalarioEventoDestinoCredito.map("B"));
        assertEquals(ContaSalarioEventoDestinoCredito.OUTRA_IF, ContaSalarioEventoDestinoCredito.map("C"));
        assertEquals(ContaSalarioEventoDestinoCredito.CONTA_POUPANCA_SICREDI, ContaSalarioEventoDestinoCredito.map("D"));
        assertEquals(ContaSalarioEventoDestinoCredito.CONTA_DIGITAL_SICREDI, ContaSalarioEventoDestinoCredito.map("W"));
        assertEquals(ContaSalarioEventoDestinoCredito.OUTRO, ContaSalarioEventoDestinoCredito.map("99"));
    }

    @Test
    public void testMapInvalidCodigo() {
        assertEquals(ContaSalarioEventoDestinoCredito.OUTRO, ContaSalarioEventoDestinoCredito.map("invalido"));
    }
}