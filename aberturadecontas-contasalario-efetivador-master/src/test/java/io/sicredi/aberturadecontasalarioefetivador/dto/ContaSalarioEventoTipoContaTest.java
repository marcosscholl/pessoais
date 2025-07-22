package io.sicredi.aberturadecontasalarioefetivador.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContaSalarioEventoTipoContaTest {

    @Test
    public void testGetCodigo() {
        assertEquals("01", ContaSalarioEventoTipoConta.CONTA_CORRENTE_INDIVIDUAL.getCodigo());
        assertEquals("02", ContaSalarioEventoTipoConta.CONTA_POUPANCA_INDIVIDUAL.getCodigo());
        assertEquals("03", ContaSalarioEventoTipoConta.CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF.getCodigo());
        assertEquals("11", ContaSalarioEventoTipoConta.CONTA_CORRENTE_CONJUNTA.getCodigo());
        assertEquals("12", ContaSalarioEventoTipoConta.CONTA_POUPANCA_CONJUNTA.getCodigo());
        assertEquals("99", ContaSalarioEventoTipoConta.CONTA_PAGAMENTO.getCodigo());
    }

    @Test
    public void testGetDescricao() {
        assertEquals("CONTA_CORRENTE_INDIVIDUAL", ContaSalarioEventoTipoConta.CONTA_CORRENTE_INDIVIDUAL.getDescricao());
        assertEquals("CONTA_POUPANCA_INDIVIDUAL", ContaSalarioEventoTipoConta.CONTA_POUPANCA_INDIVIDUAL.getDescricao());
        assertEquals("CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF", ContaSalarioEventoTipoConta.CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF.getDescricao());
        assertEquals("CONTA_CORRENTE_CONJUNTA", ContaSalarioEventoTipoConta.CONTA_CORRENTE_CONJUNTA.getDescricao());
        assertEquals("CONTA_POUPANCA_CONJUNTA", ContaSalarioEventoTipoConta.CONTA_POUPANCA_CONJUNTA.getDescricao());
        assertEquals("CONTA_PAGAMENTO", ContaSalarioEventoTipoConta.CONTA_PAGAMENTO.getDescricao());
    }

    @Test
    public void testMapValidCodigo() {
        assertEquals(ContaSalarioEventoTipoConta.CONTA_CORRENTE_INDIVIDUAL, ContaSalarioEventoTipoConta.map("01"));
        assertEquals(ContaSalarioEventoTipoConta.CONTA_POUPANCA_INDIVIDUAL, ContaSalarioEventoTipoConta.map("02"));
        assertEquals(ContaSalarioEventoTipoConta.CONTA_DIGITAL_SICREDI_OU_CONTA_PAGAMENTO_OUTRA_IF, ContaSalarioEventoTipoConta.map("03"));
        assertEquals(ContaSalarioEventoTipoConta.CONTA_CORRENTE_CONJUNTA, ContaSalarioEventoTipoConta.map("11"));
        assertEquals(ContaSalarioEventoTipoConta.CONTA_POUPANCA_CONJUNTA, ContaSalarioEventoTipoConta.map("12"));
        assertEquals(ContaSalarioEventoTipoConta.CONTA_PAGAMENTO, ContaSalarioEventoTipoConta.map("99"));
    }

    @Test
    public void testMapInvalidCodigo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ContaSalarioEventoTipoConta.map("invalido"));
        assertEquals("Código informado inválido : invalido", exception.getMessage());
    }
}