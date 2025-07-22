package io.sicredi.aberturadecontaslegadooriginacao.exception;

public class BuscaDocumentosNoAcquisitionRegisterDataException extends RuntimeException{
    public BuscaDocumentosNoAcquisitionRegisterDataException(Exception e) {
        super("Erro ao buscar os documentos do pedido no [ acquisition-register-data ].", e);
    }
}