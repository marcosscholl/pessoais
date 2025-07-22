package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class BuscaDadosClienteNoCustomerDataException extends RuntimeException{
    public BuscaDadosClienteNoCustomerDataException(Exception e) {
        super("Erro ao buscar dados do cliente na api customer-data", e);
    }
}