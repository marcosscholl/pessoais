package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.*;
import io.sicredi.aberturadecontasalarioefetivador.factories.CanalFactory;
import io.sicredi.aberturadecontasalarioefetivador.factories.TransactionIdFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderServiceTest {

    @Mock
    private CanalService canalService;
    @InjectMocks
    private HeaderService headerService;
    private static final String DOCUMENTO = "12345678901";
    private static final String CANAL_TESTE = "CANAL_TESTE";
    private static final String TRANSACTION_ID_INVALIDO = "123";

    @Test
    @DisplayName("Deve validar com sucesso")
    void deveValidarComSucesso() {
        var canal = CanalFactory.canalValido();
        when(canalService.consultarCanalAtivo(canal.getCodigo())).thenReturn(Optional.of(canal));

        headerService.validarHeaderSolicitacao(TransactionIdFactory.transactionIdValido(canal.getCodigo()), canal.getNome());

        verifyConsultaCanalAtivo(canal);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDObrigatorioException quando TransactionId estiver em branco")
    void deveLancarTransactionIDObrigatorioException() {
        assertThrows(TransactionIDObrigatorioException.class,
                () -> headerService.validarHeaderSolicitacao("", CANAL_TESTE));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDForaDoPadraoException quando TransactionId possuir um formato inválido")
    void deveLancarTransactionIDForaDoPadraoException() {
        assertThrows(TransactionIDForaDePadraoException.class,
                () -> headerService.validarHeaderSolicitacao(TRANSACTION_ID_INVALIDO, CANAL_TESTE));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDComDataInvalidaException quando data que compõe o TransactionId for inválida")
    void deveRetornarTransactionIDComDataInvalidaException() {
        var canal = CanalFactory.canalValido();
        var transactionId = "2024301011119999999999999999";
        var canalHeader = canal.getNome();

        assertThrows(TransactionIDComDataInvalidaException.class,
                () -> headerService.validarHeaderSolicitacao(transactionId, canalHeader));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar CanalNaoEncontradoOuInvalidoException quando consulta do canal retornar vazio")
    void deveLancarCanalNaoEncontradoOuInvalidoException() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());
        var canalHeader = canal.getNome();

        when(canalService.consultarCanalAtivo(canal.getCodigo())).thenReturn(Optional.empty());

        assertThrows(CanalNaoEncontradoOuInatvoException.class,
                () -> headerService.validarHeaderSolicitacao(transactionId, canalHeader));

        verifyConsultaCanalAtivo(canal);
    }

    @Test
    @DisplayName("Deve lançar CanalSemCorrespondenciaException quando canal do TransactionId for diferente do canal consultado")
    void deveLancarCanalSemCorrespondenciaException() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());
        when(canalService.consultarCanalAtivo(canal.getCodigo())).thenReturn(Optional.of(canal));

        assertThrows(CanalSemCorrespondenciaException.class,
                () -> headerService.validarHeaderSolicitacao(transactionId, CANAL_TESTE));

        verifyConsultaCanalAtivo(canal);
    }

    @Test
    @DisplayName("Deve validar TransactionId através do código e documento")
    void deveValidarTransactionIdPorCodigoEDocumento() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());
        when(canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO))
                .thenReturn(Optional.of(canal));

        var result = headerService.validarTransactionIdPorCodigoEDocumento(transactionId, DOCUMENTO);

        assertNotNull(result);
        assertEquals(canal, result);
        verifyConsultaCanalAtivoPorCodigoEDocumento(canal);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDObrigatorioException quando TransactionId for nulo")
    void deveRetornarErroQuandoTransactionIdNulo_ValidarTransactionIdPorCodigoEDocumento() {
        assertThrows(TransactionIDObrigatorioException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumento(null, DOCUMENTO));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDForaDoPadraoException quando TransactionId possuir um formato inválido")
    void deveRetornarErroQuandoTransactionIdFormatoInvalido_ValidarTransactionIdPorCodigoEDocumento() {
        assertThrows(TransactionIDForaDePadraoException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumento(TRANSACTION_ID_INVALIDO, DOCUMENTO));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDComDataInvalidaException quando data que compõe o TransactionId for inválida")
    void deveRetornarErroQuandoDataInvalida_ValidarTransactionIdPorCodigoEDocumento() {
        assertThrows(TransactionIDComDataInvalidaException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumento("2024301011119999999999999999", DOCUMENTO));

        verifyNoInteractions(canalService);
    }

    @Test
    @DisplayName("Deve lançar CanalNaoEncontradoOuInvalidoException quando consulta do canal retornar vazio")
    void deveRetornarErroQuandoCanalNaoEncontrado_ValidarTransactionIdPorCodigoEDocumento() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());
        when(canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO))
                .thenReturn(Optional.empty());

        assertThrows(CanalNaoEncontradoOuInatvoException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumento(transactionId, DOCUMENTO));

        verifyConsultaCanalAtivoPorCodigoEDocumento(canal);
    }

    @Test
    @DisplayName("Deve validar TransactionId através do código e documento")
    void deveValidarTransactionIdPorCodigoEDocumentoECanalComSucesso() {
        var canal = CanalFactory.canalValido();

        when(canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO))
                .thenReturn(Optional.of(canal));

        headerService.validarTransactionIdPorCodigoEDocumentoECanal(TransactionIdFactory.transactionIdValido(canal.getCodigo()),
                canal.getNome(), DOCUMENTO);

        verifyConsultaCanalAtivoPorCodigoEDocumento(canal);
    }

    @Test
    @DisplayName("Deve lançar CanalSemCorrespondenciaException quando canal do TransactionId for diferente do canal consultado")
    void deveRetornarErroQuandoNomeCanalNaoCorresponde_ValidarTransactionIdPorCodigoEDocumentoECanal() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());

        when(canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO))
                .thenReturn(Optional.of(canal));

        assertThrows(CanalSemCorrespondenciaException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumentoECanal(transactionId, CANAL_TESTE, DOCUMENTO));

        verifyConsultaCanalAtivoPorCodigoEDocumento(canal);
    }

    @Test
    @DisplayName("Deve lançar CanalNaoEncontradoOuInvalidoException quando consulta do canal retornar vazio")
    void deveRetornarErroQuandoCanalNaoEncontrado_ValidarTransactionIdPorCodigoEDocumentoECanal() {
        var canal = CanalFactory.canalValido();
        var transactionId = TransactionIdFactory.transactionIdValido(canal.getCodigo());
        var canalHeader = canal.getNome();

        when(canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO))
                .thenReturn(Optional.empty());

        assertThrows(CanalNaoEncontradoOuInatvoException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumentoECanal(transactionId, canalHeader, DOCUMENTO));

        verifyConsultaCanalAtivoPorCodigoEDocumento(canal);
    }

    @Test
    @DisplayName("Deve lançar TransactionIDForaDoPadraoException quando TransactionId possuir um formato inválido")
    void deveRetornarErroQuandoTransactionIdInvalido_ValidarTransactionIdPorCodigoEDocumentoECanal() {
        assertThrows(TransactionIDForaDePadraoException.class,
                () -> headerService.validarTransactionIdPorCodigoEDocumentoECanal(TRANSACTION_ID_INVALIDO, CANAL_TESTE, DOCUMENTO));

        verifyNoInteractions(canalService);
    }

    private void verifyConsultaCanalAtivo(Canal canal) {
        verify(canalService, times(1)).consultarCanalAtivo(canal.getCodigo());
    }

    private void verifyConsultaCanalAtivoPorCodigoEDocumento(Canal canal) {
        verify(canalService, times(1)).consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), DOCUMENTO);
    }
}