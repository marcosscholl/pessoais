package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class BuscaPedidoNoAcquisitionOrdersException extends RuntimeException{
    public BuscaPedidoNoAcquisitionOrdersException(Exception e) {
        super("Erro ao buscar pedido no acquisition-orders-v1", e);
    }
}