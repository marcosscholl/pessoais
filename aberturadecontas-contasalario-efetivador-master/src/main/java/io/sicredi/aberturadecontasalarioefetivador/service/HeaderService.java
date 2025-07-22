package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class HeaderService {

    public static final String PADRAO_TRANSACTION_ID = "^(\\d{8})(\\d{4})(\\d{16})$";
    private final CanalService canalService;

    public void validarHeaderSolicitacao(String transactionId, String canalHeader) {
        Matcher matcher = validarTransactionIdERegex(transactionId);

        var canalOpt = canalService.consultarCanalAtivo(Long.valueOf(matcher.group(2)));
        if (canalOpt.isEmpty()) throw new CanalNaoEncontradoOuInatvoException();
        if (!canalOpt.get().getNome().equalsIgnoreCase(canalHeader)) throw new CanalSemCorrespondenciaException();
    }

    public Canal validarTransactionIdPorCodigoEDocumento(String transactionId, String documento) {
        Matcher matcher = validarTransactionIdERegex(transactionId);
        var canalOpt = canalService.consultarCanalAtivoPorCodigoEDocumento(Long.valueOf(matcher.group(2)), documento);
        if (canalOpt.isEmpty()) throw new CanalNaoEncontradoOuInatvoException();
        return canalOpt.get();
    }
    public void validarTransactionIdPorCodigoEDocumentoECanal(String transactionId, String canalHeader, String documento) {
        Canal canal = validarTransactionIdPorCodigoEDocumento(transactionId, documento);
        if (!canal.getNome().equalsIgnoreCase(canalHeader)) throw new CanalSemCorrespondenciaException();
    }

    private static Matcher validarTransactionIdERegex(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) throw new TransactionIDObrigatorioException();

        var transactionIdPattern = Pattern.compile(PADRAO_TRANSACTION_ID);
        var matcher = transactionIdPattern.matcher(transactionId);

        if (!matcher.matches()) throw new TransactionIDForaDePadraoException();

        try {
            LocalDate.parse(matcher.group(1), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new TransactionIDComDataInvalidaException(e);
        }
        return matcher;
    }
}
